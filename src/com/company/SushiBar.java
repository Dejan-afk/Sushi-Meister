package com.company;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class SushiBar {
    private ReentrantLock lock;
    private BlockingQueue<Integer> blockingQueue;
    private ArrayList<String> progress;
    private int totalNumberOfGuests;
    private int totalNumberOfRejectedGuests;
    private Semaphore sem;
    private int ID = 0;

    SushiBar(int seatings){
        blockingQueue = new ArrayBlockingQueue<Integer>(seatings, true); //fair
        this.progress = new ArrayList<>();
        this.totalNumberOfGuests = 0;
        this.totalNumberOfRejectedGuests = 0;
        lock = new ReentrantLock(true);
        sem = new Semaphore(seatings);
    }

    public boolean entry(GuestGroup guestGroup){
        int numberOfIncomingGuests = guestGroup.getGuestCount();
        if(sem.tryAcquire(numberOfIncomingGuests)){
            lock.lock();
            guestGroup.setID(++ID);  //Relevant, wenn man mit Datenbanken arbeiten sollte, hier nur für blockingQueue.
            for(int i = 0; i < numberOfIncomingGuests; i++){
                try {
                    blockingQueue.put(guestGroup.getID());
                }catch (InterruptedException e){}

            }
            this.progress.add(guestGroup.getGroupName()+" betritt um "+ getTime()+ " mit "+numberOfIncomingGuests+" Personen die Bar");
            this.totalNumberOfGuests += numberOfIncomingGuests;
            lock.unlock();
        }else{
            return false;
        }
        return true;
    }

    public int leave(GuestGroup guestGroup){
        int numberOfLeavingGuests = guestGroup.getGuestCount();
        lock.lock();
        for(int i = 0; i < numberOfLeavingGuests; i++){
            try {
                blockingQueue.take();
            }catch (InterruptedException e){}
            sem.release();
        }
        this.progress.add(guestGroup.getGroupName()+" verlässt um " +getTime()+" mit "+numberOfLeavingGuests+" Personen die Bar.");
        lock.unlock();
        return numberOfLeavingGuests;
    }

    public void strictLeave(GuestGroup guestGroup){
        int count = leave(guestGroup);
        this.totalNumberOfGuests -= count;
        this.totalNumberOfRejectedGuests += count;
        this.progress.set(this.progress.size()-1, guestGroup.getGroupName()+ " wurden gebeten die Bar zu verlassen ohne Bedienung: "+count+" Personen");
    }

        private String getTime(){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            return dtf.format(now);
        }

    public int getTotalNumberOfGuests(){return this.totalNumberOfGuests;}
    public int getTotalNumberOfRejectedGuests(){return this.totalNumberOfRejectedGuests;}
    public ArrayList<String> getProtocol(){return this.progress;}
}
