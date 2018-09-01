#!/bin/bash

##### Constants

right_now=$(date +"%x %r %Z")

show_menu(){
    NORMAL=`echo "\033[m"`
    MENU=`echo "\033[36m"` #Blue
    NUMBER=`echo "\033[33m"` #yellow
    FGRED=`echo "\033[41m"`
    RED_TEXT=`echo "\033[31m"`
    ENTER_LINE=`echo "\033[33m"`
    
    
    echo -e "${MENU}*********************************************${NORMAL}"
    echo -e "${MENU}** ${NORMAL}AWS MetaData 2018 - @rwnevinger @ramiel ${MENU}**${NORMAL}"
    echo -e "${MENU}*********************************************${NORMAL}"
    echo -e "${MENU}** ${NORMAL} Running on $USER@$HOSTNAME - $right_now ${MENU}**${NORMAL}"
    echo -e "${MENU}*********************************************${NORMAL}"
    echo -e "${MENU}**${NUMBER} 1)${MENU} Create Gateway ${NORMAL}"
    echo -e "${MENU}**${NUMBER} 2)${MENU} Delete Gateway ${NORMAL}"
    echo -e "${MENU}**${NUMBER} 3)${MENU} Create Route ${NORMAL}"
    echo -e "${MENU}**${NUMBER} 4)${MENU} Delete Route ${NORMAL}"
    echo -e "${MENU}**${NUMBER} 5)${MENU} run Lambda${NORMAL}"
    echo -e "${MENU}*********************************************${NORMAL}"
    echo -e "${ENTER_LINE}Please enter a menu option and enter or ${RED_TEXT}enter to exit. ${NORMAL}"
    read opt
}

function option_picked() {
    COLOR='\033[01;31m' # bold red
    RESET='\033[00;00m' # normal white
    MESSAGE=${@:-"${RESET}Error: No message passed"}
    echo -e "${COLOR}${MESSAGE}${RESET}"
}

clear
show_menu
while [ opt != '' ]
    do
    if [[ $opt = "" ]]; then
            exit;
    else
        case $opt in
        1) clear;
        option_picked "Create a Gateway";
        # use something like this to read in the gateway and other variables maybe set defaults
   
       # read -p "Select a VPC: " vpc;
        #read -p "Select a subnet: " sub;
        #read -p "Select a rtb: " rtb; 
        #read -p "Select a mask [0.0.0.0/0]: " mask;
        #java -cp .:target/AWSMetadata.jar com.bauenecp.CreateNatGateway $vpc $sub $rtb $mask;
        java -version;
        sleep 10;
        menu;
        ;;
        2) clear;
            option_picked "Option 2 Picked";
            sudo mount /dev/sdi1 /mnt/usbDrive; 
        menu;
            ;;

        3) clear;
            option_picked "Option 3 Picked";
        sudo service apache2 restart;
            show_menu;
            ;;

        4) clear;
            option_picked "Option 4 Picked";
           ssh lmesser@ -p 2010;
            show_menu;
            ;;
         5) clear;
            option_picked "Option 5 Picked";
           ssh lmesser@ -p 2010;
            show_menu;
            ;; 
        x)exit;
        ;;
        \n)exit;
        ;;

        *)clear;
        option_picked "Pick an option from the menu";
        show_menu;
        ;;
    esac
fi
done
