package hophacks.omarkadry.textingmerge;

/**
 * Created by Omar Kadry on 2/7/2015.
 * Wrapper Class for Contact
 */
public class Contact {
    private String phoneNumber;
    private String name;

    public Contact(String phoneNumber, String name){
        this.phoneNumber = phoneNumber;
        this.name = name;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public String name(){
        return name;
    }

    public String toString(){
        return "Name: " + name + " Phone Number: " + phoneNumber;
    }
}
