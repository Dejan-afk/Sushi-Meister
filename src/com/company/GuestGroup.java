package com.company;

import java.util.Scanner;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

public class GuestGroup implements Runnable{
    private volatile int guestCount;
    private int groupID;
    SushiBar bar;
    private String groupName;
    private Semaphore awaitInputLock;
    private Scanner scanner;
    private boolean entrySuccess;
    private int delay;

    GuestGroup(SushiBar restaurant, String name, Semaphore lock, int delay){
        this.bar = restaurant;
        this.guestCount = randomNumberOfGuests();
        this.groupName = name;
        awaitInputLock = lock;
        scanner = new Scanner(System.in);
        this.delay = delay * 2250 + 1000;
    }

    @Override
    public void run() {
        //ANKUNFT
        try {
            sleep(randomTimeOfArriving());
        } catch (InterruptedException e) {}


        //EINBUCHUNG
        try {
            awaitInputLock.acquire();
        }catch (InterruptedException e){}
        System.out.println(this.groupName + ": Hallo, sind Plätze für "+this.guestCount+" frei?");
        entrySuccess = bar.entry(this);

        if(!entrySuccess) {
            System.out.println("Chef: Nein, tut mir Leid.");
            System.out.println(this.groupName+": Danke, auf Wiedersehen. Ein ander Mal :-)");
            awaitInputLock.release();
            return;
        }

        System.out.println("Möchten sie die " + this.groupName + " mit "+this.guestCount+" Personen buchen und an den Tisch geleiten?  [y/n]");
        boolean theInputIsRight;
        do{
            String input = scanner.nextLine();
            theInputIsRight = input.contains("y") || input.contains("n");
            if(!theInputIsRight){
                System.out.println("Falscher Input, bitte erneut versuchen mit einer der beiden Optionen: [ y / n ]");
            }else{
                if(input.contains("y")){
                    System.out.println("Haben wir, kommt rein!");
                    awaitInputLock.release();
                }else{
                    System.out.println("Ihr schon wieder?! Nein, ihr kommt nicht herein, verschwindet. ");
                    bar.strictLeave(this);
                    awaitInputLock.release();
                    return;
                }
            }
        }while(!theInputIsRight);



        //ESSEN
        System.out.println("Chef: Am kochen...");
            try {
                sleep(randomTimeOfDining());
            } catch (InterruptedException e) {}


        //AUSBUCHUNG
        try {
            awaitInputLock.acquire();
        }catch (InterruptedException e){}

        System.out.println(this.groupName+": Danke für das Essen, wir würden gerne zahlen.");
        System.out.println("Zum Ausbuchen mit Eingabe bestätigen.");
        scanner.nextLine();
        System.out.println("Chef: Danke und auf Wiedersehen!");
        awaitInputLock.release();

        //VERLASSEN
        bar.leave(this);
    }

    public void setID(int groupID){this.groupID = groupID;}
    public int getID(){return this.groupID;}

    public int randomNumberOfGuests(){
        return (int)(Math.random()*4)+1;
    }
    public int randomTimeOfDining(){
        return (int)(Math.random()*10000+5000);
    }
    public int randomTimeOfArriving(){return (int)(Math.random()*20000+this.delay);}
    public int getGuestCount(){return this.guestCount; }
    public String getGroupName(){return this.groupName;}
}