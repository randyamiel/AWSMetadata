package com.bauenecp;

import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;

//  you will find the majority of resourcces here
import com.amazonaws.services.ec2.model.*;

import java.io.*;
import java.text.*;
import java.util.*;


/**
 * DeleteNatGateway
 * Requires the removal of two resources
 * NatGateway and Route
 *
 */
public class DeleteNatGateway {


public String VPCID = new String();
public String NATGATEWAYID = new String();
public String ROUTETABLEID = new String();
public String CIDRBLOCK = new String();
public String AID = new String();
public String PIP = new String();
public String action = new String();
public Hashtable<String,String> AllocatedEIP = new Hashtable<String,String>();

public AmazonEC2 client = AmazonEC2ClientBuilder.standard().build();

  public boolean read() {

    System.out.println("Begin read()");
    boolean activeGateway = false;

    try {

      DescribeNatGatewaysRequest request = new DescribeNatGatewaysRequest().withFilter(new Filter().withName("vpc-id").withValues(this.VPCID));
      DescribeNatGatewaysResult result = client.describeNatGateways(request);

      //System.out.println(result.getNatGateways().isEmpty());
      //System.out.println(result.getNatGateways().size());
      if (result.getNatGateways().isEmpty()) {
        throw new Exception("VPC " + this.VPCID + " does not have an attached NatGateway ");
      }

      for (NatGateway nat: result.getNatGateways()) {
        // read state
        if ("available".equals(nat.getState())) {
          // read gateway id
          this.NATGATEWAYID = nat.getNatGatewayId();
          System.out.println("**** NatGateway " + this.NATGATEWAYID + " will be DELETED ****");
          activeGateway = true;
          // read NatGatewayAddress
          for (NatGatewayAddress natAddress: nat.getNatGatewayAddresses()) {
            // read eip id
            this.AID = natAddress.getAllocationId();
            this.PIP  = natAddress.getPublicIp();
            System.out.println("**** Allocation ID " + this.AID + " will be DISASSOCIATED by Lambda ReleaseEIP ****");
            System.out.println("**** Public IP " + this.PIP + " will be RELEASED by Lambda ReleaseEIP ****");
          }
        }
        else {
         System.out.println(nat.getNatGatewayId());
         System.out.println(nat.getState());
         System.out.println(nat.getCreateTime().toString());
         System.out.println(nat.getSubnetId());
        }
      }
        
    }
    catch (Exception e) {
       System.out.println("read() exception " + e.toString());
       System.out.println("End read()");
       return false;
    }


    if(activeGateway) {
      System.out.println("End read()");
      return true;
    }
    else {
      System.out.println("No Active NatGateways running");
      System.out.println("End read()");
      return false;
    }

  }

  /*
   * delete network address translation gateway
   *
  */
  public boolean delete() {

    System.out.println("Begin delete()");

    try {

      DeleteNatGatewayRequest request = new DeleteNatGatewayRequest().withNatGatewayId(this.NATGATEWAYID);
      DeleteNatGatewayResult result = client.deleteNatGateway(request);
      System.out.println("Deleted NatGateway " + result.toString());

    }
    catch (Exception e) {
       System.out.println("exception" + e.toString());
       return false;
    }

    System.out.println("End delete()");
    return true;
  }

  /*
   * delete nat gateway route from specified route table
   *
  */
  public void deleteRoute() {

    System.out.println("Begin deleteRoute()");

    try {
      // javadoc
      // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/ec2/AmazonEC2Client.html
      // --destination-cidr-block 0.0.0.0/0
      //DeleteRouteRequest request = new DeleteRouteRequest().withRouteTableId(this.ROUTETABLEID).withDestinationCidrBlock("0.0.0.0/0");
      DeleteRouteRequest request = new DeleteRouteRequest().withRouteTableId(this.ROUTETABLEID).withDestinationCidrBlock(this.CIDRBLOCK);
      DeleteRouteResult result = client.deleteRoute(request);

      System.out.println("Deleted " + ROUTETABLEID + " " + result.toString());

    }
    catch (Exception e) {
       System.out.println("exception" + e.toString());
    }

    System.out.println("End deleteRoute()");
  }


  /*
   * read all addresses disassociate ip addresses 
   *
  */
  public void disassociateEIP() {

      System.out.println("Begin disassociateEIP()");

      try {

          DescribeAddressesRequest describeRequest = new DescribeAddressesRequest();
          DescribeAddressesResult eipResult = client.describeAddresses(describeRequest);

          for (Address address: eipResult.getAddresses()) {
              if(AllocatedEIP.containsKey(address.getPublicIp())) {
                System.out.println("public ip " + address.getPublicIp());
                System.out.println("association " + address.getAssociationId());
                System.out.println("allocation " + address.getAllocationId());
                System.out.println("domain " + address.getDomain());
                // TODO add address.getDomain().equals("vpc") check;  only vpc's are disassocaited by associationid else ip address
                if(address.getAssociationId() != null) {
                  //DisassociateAddressRequest request = new DisassociateAddressRequest().withAssociationId(address.getAssociationId());
                  DisassociateAddressRequest request = new DisassociateAddressRequest();
                                             request.setAssociationId(address.getAssociationId());
                  DisassociateAddressResult result = client.disassociateAddress(request);
                  System.out.println("disassocation result " + result.toString());
               }
             }
             else if (address.getAssociationId() == null) {
               System.out.println("**** Non Associated public ip " + address.getPublicIp() + " --> allocation id " + address.getAllocationId());
               ReleaseAddressRequest releaseRequest = new ReleaseAddressRequest().withAllocationId(address.getAllocationId());
               ReleaseAddressResult releaseResponse = client.releaseAddress(releaseRequest);
               System.out.println("**** RELEASED EIP " + address.getPublicIp() +  " " + releaseResponse.toString());
             }
          } // read next adddress

      }
      catch (Exception e) {
       System.out.println("exception" + e.toString());
      }

      System.out.println("End disassociateEIP()");
  }


  /*
   * read all addresses and release all eip addresses 
   *
  */
  public void releaseEIP() {

      System.out.println("Begin releaseEIP()");

      try {

          DescribeAddressesRequest describeRequest = new DescribeAddressesRequest();
          DescribeAddressesResult eipResult = client.describeAddresses(describeRequest);

          for (Address address: eipResult.getAddresses()) {
             if (address.getAssociationId() == null) {
               System.out.println("**** Non Associated public ip " + address.getPublicIp() + " --> allocation id " + address.getAllocationId());
               //System.out.println("public ip " + address.getPublicIp());
               //System.out.println("association " + address.getAssociationId());
               //System.out.println("allocation " + address.getAllocationId());
               //System.out.println("domain " + address.getDomain());
               ReleaseAddressRequest releaseRequest = new ReleaseAddressRequest().withAllocationId(address.getAllocationId());
               ReleaseAddressResult releaseResponse = client.releaseAddress(releaseRequest);
               System.out.println("**** RELEASED EIP " + address.getPublicIp() +  " " + releaseResponse.toString());
             }
          } // read next adddress

      }
      catch (Exception e) {
       System.out.println("exception" + e.toString());
      }

      System.out.println("End releaseEIP()");
  }


  public void environment() {
    System.out.println("Begin environment()");

    // initialize map with all environment keys and values
    Map<String, String> ENV = System.getenv();

    // define list
    List<String> envKeys = new ArrayList<String>();

    // initialize list
    for (String envKey : ENV.keySet()) {
        envKeys.add(envKey);
    }

    // sort list
    Collections.sort(envKeys);

    // print
    for (String envKey : envKeys) {
        System.out.println(envKey + "->" + ENV.get(envKey));
        //if(envKey.equals("HOST")) {
        //  this.HOST = ENV.get(envKey);
        //}
    }

    System.out.println("End environment()");
  }

  public void main(Context context) {

    System.out.println("Begin main()");

    DeleteNatGateway NAT = new DeleteNatGateway();

    try {

      if(System.getenv().containsKey("VpcId")) {
        NAT.VPCID = System.getenv("VpcId");
        System.out.println("setting VPCID " + NAT.VPCID);
      }
      else {
        throw new Exception("Lambda VpcId environment key and value required");
      }

      if(System.getenv().containsKey("RouteTableId")) {
        NAT.ROUTETABLEID = System.getenv("RouteTableId");
        System.out.println("setting ROUTETABLEID " + NAT.ROUTETABLEID);
      }
      else {
        throw new Exception("Lambda RouteTableId environment key and value required");
      }

      if(System.getenv().containsKey("CidrBlock")) {
        NAT.CIDRBLOCK = System.getenv("CidrBlock");
        System.out.println("setting CIDRBLOCK " + NAT.CIDRBLOCK);
      }
      else {
        throw new Exception("Lambda CidrBlock environment key and value required");
      }

      if (NAT.read()) {
        if(NAT.delete()) {
           NAT.deleteRoute();
        }

      }
      else {
           NAT.releaseEIP();
      }

    }
    catch (Exception e) {
       System.out.println("main handler exception" + e.toString());
    }

    System.out.println("End main()");

  }

  public static void main(String[] args) {

    System.out.println("Begin main.static()");

    DeleteNatGateway NAT = new DeleteNatGateway();

    NAT.VPCID = args[0];
    NAT.ROUTETABLEID = args[1];
    NAT.CIDRBLOCK = args[2];

    System.out.println("setting VPCID " + NAT.VPCID);
    System.out.println("setting ROUTETABLEID " + NAT.ROUTETABLEID);
    System.out.println("setting CIDRBLOCK " + NAT.CIDRBLOCK);

    if (NAT.read()) {
        if(NAT.delete()) {
           NAT.deleteRoute();
        }
    }
    else {
           NAT.releaseEIP();
    }

    System.out.println("End main.static()");

  }

}
