#!/bin/bash

##### Constants

host="System Information for $HOSTNAME"
right_now=$(date +"%x %r %Z")
running_user="$USER"
HEIGHT=15
WIDTH=40
CHOICE_HEIGHT=4
BACKTITLE="AWS MetaData 2018 - @rwnevinger @ramiel"
TITLE="AWS Run Menu - $USER@$HOSTNAME"
MENU="Choose one of the following options:"

OPTIONS=(1 "CreateGateway"
         2 "DeleteGateway"
         3 "CreateRoute"
         4 "DeleteRoute"
         5 "runLambda")

CHOICE=$(dialog --clear \
                --backtitle "$BACKTITLE" \
                --title "$TITLE" \
                --menu "$MENU" \
                $HEIGHT $WIDTH $CHOICE_HEIGHT \
                "${OPTIONS[@]}" \
                2>&1 >/dev/tty)

clear
case $CHOICE in
        1)
            #TODO read in gateway
            echo "Creating Gateway..."
            ;;
        2)
          #TODO read in gateway
            echo "Deleting Gateway..."
            ;;
        3)
            echo "Create Route..."
            ;;
esac
