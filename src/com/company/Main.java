package com.company;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Main {

    public static void main(String[] args) {
        int GROUPOFGUESTS;
        boolean theInputIsRight;
        Scanner scanner = new Scanner(System.in);
        do{
            do {
                System.out.println("Was ist dein heutiges Ziel an Kundengruppen?");
                while (!scanner.hasNextInt()) {
                    System.out.println("Das ist keine Nummer, bitte nochmal!");
                    scanner.next();
                }
                GROUPOFGUESTS = scanner.nextInt();
                if(GROUPOFGUESTS <= 0) System.out.println("Mit "+GROUPOFGUESTS+" Kundengruppen wirst du keinen Umsatz generieren können, überwinde deinen Schweinehund und versuche es bitte nocheinmal: ");
            } while (GROUPOFGUESTS <= 0);
            scanner.nextLine();
            SushiMeister sushiMeister = new SushiMeister(GROUPOFGUESTS);
            Thread t = new Thread(sushiMeister);
            t.start();
            try {
                t.join();
            }catch (InterruptedException e){}
            String input = "";
            System.out.println("Wenn du nochmal arbeiten möchtest drücke: y");
            input = scanner.nextLine();
            theInputIsRight = input.contains("y");
        }while(theInputIsRight);


        System.out.println("Genau, entspann dich lieber erstmal! :-)");
    }
}

class SushiMeister implements Runnable{
    private SushiBar bar;
    private int seatings;
    private int amountOfGuests;
    private Semaphore awaitInputLock;

    SushiMeister(int guests){
        this.amountOfGuests = guests;
        this.awaitInputLock = new Semaphore(1);
    }

    @Override
    public void run(){
        Scanner myInput = new Scanner(System.in);
        //Arbeitsverlauf
        do {
            System.out.println("Wie viele Plätze möchtest du heute zur Verfügung stellen?");
            while (!myInput.hasNextInt()) {
                System.out.println("Das ist keine Nummer, bitte nochmal!");
                myInput.next();
            }
            this.seatings = myInput.nextInt();
            if(this.seatings <= 0) System.out.println("Mit "+this.seatings+" Sitzplätzen wirst du keinen Umsatz generieren können, überwinde deinen Schweinehund und versuche es bitte nocheinmal: ");
        } while (this.seatings <= 0);
        this.bar = new SushiBar(this.seatings);

        Thread guests[] = new Thread[this.amountOfGuests];

        for(int i = 0; i < this.amountOfGuests; i++){
            guests[i] = new Thread(new GuestGroup(this.bar, "Gruppe-"+String.valueOf(i), this.awaitInputLock, i+1));
        }
        System.out.println("Warte auf Gäste...");
        for(var guest: guests)
            guest.start();

        for(var guest: guests) {
            try{
                guest.join();
            }catch (InterruptedException e) {}
        }

        //clear Scanner
        myInput.nextLine();

        //Datenabfrage
        boolean theInputIsRight, printProtocol = true;
        do {
            String input = "";
            System.out.println("Möchtest du das Tagesprotokoll drucken? [y/n] ");
            input = myInput.nextLine();
            theInputIsRight = input.contains("y") || input.contains("n");
            if (!theInputIsRight) {
                System.out.println("Falscher Input, bitte erneut versuchen mit einer der beiden Optionen: [ y / n ]");
            } else {
                if (input.contains("y")) {
                    printProtocol = true;
                } else {
                    printProtocol = false;
                }
            }
        }while (!theInputIsRight) ;


            //Datenausgabe
            if (printProtocol) {
                var protocol = this.bar.getProtocol();
                System.out.println("Anbei das Tagesprotokoll: ");
                for (int i = 0; i < protocol.size(); i++) {
                    System.out.println(protocol.get(i));
                }
                System.out.println("Insgesamt wurden " + this.bar.getTotalNumberOfGuests() + " Kunden bedient");
                System.out.println("Des Weiteren wurden " + this.bar.getTotalNumberOfRejectedGuests() + " Kunden des Zuganges verwährt.");
            }

            System.out.println("So schnell kann ein Arbeitstag vergehen, wenn man in den Flow kommt. Das genügt für heute, ich schließe ab. Gute Nacht!");
        }
}