package hophacks.omarkadry.textingmerge;

/**
 * Created by Omar Kadry on 2/7/2015.
 * Wrapper Class for Contact
 */
public class Contact {
    private String phoneNumber;
    private String first_name;
    private String last_name;

    public Contact(String phoneNumber, String name)
    //splits name into first and last name
    {
        String[] tokens = name.split(" ");
        if (tokens.length != 2) {
            this.first_name = name;
            this.last_name = "";
        } else {
            this.first_name = tokens[0];
            this.last_name = tokens[1].substring(1);    //remove space
        }

        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFirstName() {
        return first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public String getFullName()
    {
        if (last_name.length() > 0)
            return first_name + " " + last_name;
        else
            return first_name;
    }

    public String toString(){
        return "Name: " + this.getFullName() + " Phone Number: " + phoneNumber;
    }
}
