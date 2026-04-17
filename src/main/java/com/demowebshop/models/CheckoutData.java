package com.demowebshop.models;

public final class CheckoutData {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String company;
    private final String country;
    private final String city;
    private final String addressLine1;
    private final String addressLine2;
    private final String zipCode;
    private final String phoneNumber;

    private CheckoutData(Builder builder) {
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.company = builder.company;
        this.country = builder.country;
        this.city = builder.city;
        this.addressLine1 = builder.addressLine1;
        this.addressLine2 = builder.addressLine2;
        this.zipCode = builder.zipCode;
        this.phoneNumber = builder.phoneNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getCompany() {
        return company;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public static final class Builder {
        private String firstName;
        private String lastName;
        private String email;
        private String company;
        private String country;
        private String city;
        private String addressLine1;
        private String addressLine2;
        private String zipCode;
        private String phoneNumber;

        private Builder() {
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder company(String company) {
            this.company = company;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder addressLine1(String addressLine1) {
            this.addressLine1 = addressLine1;
            return this;
        }

        public Builder addressLine2(String addressLine2) {
            this.addressLine2 = addressLine2;
            return this;
        }

        public Builder zipCode(String zipCode) {
            this.zipCode = zipCode;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public CheckoutData build() {
            return new CheckoutData(this);
        }
    }
}
