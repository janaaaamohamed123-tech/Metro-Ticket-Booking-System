package org.example.metro;

public class Route {
    private int id;
    private  String startStation;
    private String endStation;
    private int availableSeats;
    public Route(int id, String startStation, String endStation, int availableSeats){
        this.id = id;
        this.startStation = startStation;
         this.endStation = endStation;
          this.availableSeats = availableSeats;
    }
    public int getId(){
        return id;
    }
    public String getStartStation(){
        return startStation;
    }
    public String getEndStation(){
        return endStation;
    }
    public int getAvailableSeats(){
        return availableSeats;
    }
    public void bookSeat(){
        if (availableSeats <= 0){
            throw new IllegalStateException("No Seats available on route " + id);
        }
        availableSeats--;

    }
    public void cancelSeat(){
        availableSeats++;

    }
    @Override
    public String toString(){
        return id + "|" + startStation + "|" + endStation + "|" + availableSeats;
    }
    public String toDisplayString(){
        return "[" + id + "] " + startStation + " → " + endStation
                + "  (" + availableSeats + " seats)";

    }

    }

