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
 * CreateNatGateway
 * Requires creating two resources
 * NatGateway and Route
 *
 */
public class CreateNatGateway {


public String VPCID = new String();
public String NATGATEWAYID = new String();
public String ROUTETABLEID = new String();
public String CIDRBLOCK = new String();
public String SUBNETID= new String();
public String EIPID= new String();
public String action = new String();

public AmazonEC2 client = AmazonEC2ClientBuilder.standard().build();

  public boolean create() {

    System.out.println("Begin create()");

    try {

      //CreateNatGatewayRequest request = new CreateNatGatewayRequest().withAllocationId("eipalloc-37fc1a52").withSubnetId("subnet-1a2b3c4d");
      CreateNatGatewayRequest request = new CreateNatGatewayRequest().withAllocationId(this.EIPID).withSubnetId(this.SUBNETID);
      CreateNatGatewayResult result = client.createNatGateway(request);

      NatGateway NatGateway = result.getNatGateway();

      System.out.println(NatGateway.toString());

      boolean pendingStatus = true;

      this.NATGATEWAYID = NatGateway.getNatGatewayId();

      System.out.println("Setting class.NATGATEWAYID " + this.NATGATEWAYID);
      System.out.println("State " + NatGateway.getState());
   
      if ("available".equals(NatGateway.getState())) {
        pendingStatus = false;
      }

      // check status every 10 seconds
      while(pendingStatus) {
        Thread.sleep(10000);
        //DescribeNatGatewaysRequest describeRequest = new DescribeNatGatewaysRequest().withNatGatewayIds(this.NATGATEWAYID);
        DescribeNatGatewaysRequest describeRequest = new DescribeNatGatewaysRequest().withFilter(new Filter().withName("vpc-id").withValues(this.VPCID));
        DescribeNatGatewaysResult describeResult = client.describeNatGateways(describeRequest);
        for (NatGateway nat: describeResult.getNatGateways()) {
          if(this.NATGATEWAYID.equals(nat.getNatGatewayId())) {
              System.out.println(nat.getNatGatewayId() + ":" + nat.getState());
              if ("available".equals(nat.getState())) {
                pendingStatus = false;
              }
              else if ("failed".equals(nat.getState())) {
                throw new Exception("failed to create NatGateway");
              }
          }
        }

      }

      System.out.println("**** CREATED NatGateway " + this.NATGATEWAYID);

    }
    catch (Exception e) {
       System.out.println("exception" + e.toString());
       return false;
    }

    System.out.println("End create()");
    return true;
  }

  public void createRoute() {

    System.out.println("Begin createRoute()");

    try {
      // javadoc
      // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/ec2/AmazonEC2Client.html
      // --destination-cidr-block 0.0.0.0/0
      //DeleteRouteRequest request = new DeleteRouteRequest().withRouteTableId(this.ROUTETABLEID).withDestinationCidrBlock("0.0.0.0/0");
      CreateRouteRequest request = 
        new CreateRouteRequest().withDestinationCidrBlock(this.CIDRBLOCK).withGatewayId(this.NATGATEWAYID).withRouteTableId(this.ROUTETABLEID);
      CreateRouteResult result = client.createRoute(request);

      System.out.println("Created " + ROUTETABLEID + " " + result.toString());

    }
    catch (Exception e) {
       System.out.println("exception" + e.toString());
    }

    System.out.println("End createRoute()");
  }


  public boolean allocateEIP() {

    System.out.println("Begin allocateEIP()");

    try {

      AllocateAddressRequest request = new AllocateAddressRequest().withDomain("vpc");
      AllocateAddressResult result = client.allocateAddress(request);

      // sleep for 10 seconds...
      Thread.sleep(10000);
      this.EIPID = result.getAllocationId();

      System.out.println("**** ALLOCATED EIP " + this.EIPID);

    }
    catch (Exception e) {
       System.out.println("exception" + e.toString());
       return false;
    }

    System.out.println("End allocateEIP()");

    return true;

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

    CreateNatGateway NAT = new CreateNatGateway();

    try {

      //CreateNatGatewayRequest request = new CreateNatGatewayRequest().withAllocationId("eipalloc-37fc1a52").withSubnetId("subnet-1a2b3c4d");
      if(System.getenv().containsKey("VpcId")) {
        NAT.VPCID = System.getenv("VpcId");
        System.out.println("setting VPCID " + NAT.VPCID);
      }
      else {
        throw new Exception("Lambda VpcId environment key and value required");
      }

      /*
      if(System.getenv().containsKey("EipId")) {
        NAT.EIPID = System.getenv("EipId");
        System.out.println("setting EIPID " + NAT.EIPID);
      }
      else {
        throw new Exception("Lambda EipId environment key and value required");
      }
      */

      if(System.getenv().containsKey("SubnetId")) {
        NAT.SUBNETID = System.getenv("SubnetId");
        System.out.println("setting SubnetId " + NAT.SUBNETID);
      }
      else {
        throw new Exception("Lambda RouteTableId environment key and value required");
      }

      if(System.getenv().containsKey("RouteTableId")) {
        NAT.ROUTETABLEID = System.getenv("RouteTableId");
        System.out.println("setting RouteTableId " + NAT.ROUTETABLEID);
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

      if (NAT.allocateEIP()) {
        if (NAT.create()) {
           NAT.createRoute();
        }
      }

    }
    catch (Exception e) {
       System.out.println("main handler exception" + e.toString());
    }

    System.out.println("End main()");

  }

  public static void main(String[] args) {

    System.out.println("Begin main.static()");

    CreateNatGateway NAT = new CreateNatGateway();

    NAT.VPCID = args[0];
    NAT.SUBNETID = args[1];
    NAT.ROUTETABLEID = args[2];
    NAT.CIDRBLOCK = args[3];

    System.out.println("setting VPCID " + NAT.VPCID);
    System.out.println("setting SUBNETID " + NAT.SUBNETID);
    System.out.println("setting ROUTETABLEID " + NAT.ROUTETABLEID);
    System.out.println("setting CIDRBLOCK " + NAT.CIDRBLOCK);

    if (NAT.allocateEIP()) {
      if (NAT.create()) {
           NAT.createRoute();
      }
    }

    System.out.println("End main.static()");

  }

}
