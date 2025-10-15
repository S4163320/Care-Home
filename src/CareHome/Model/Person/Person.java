package CareHome.Model.Person;

import CareHome.Model.Gender;

public abstract class Person {
    protected String id; // Unique identifier for each person
    protected String firstName; // Person's first name
    protected String lastName; // Person's last name
    protected Gender gender; // MALE or FEMALE
    protected int age; // Person's age

    // Constructor for all persons
    public Person(String id, String firstName, String lastName, Gender gender, int age) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.age = age;
    }

    // Get full name as "FirstName LastName"
    public String getName() {
        return firstName + " " + lastName;
    }

    // Abstract method - each person type must define their role
    public abstract String getRole();

    // Getters
    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Gender getGender() { return gender; }
    public int getAge() { return age; }


    @Override
    public String toString() {
        return getRole() + ": " + getName() + " (ID: " + id + ")";
    }
}
