package mod.utils;

import mod.osm.OsmNode;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Address implements Serializable {
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^(?<street>[A-Za-zÆØÅæøå ]+) (?<houseNumber>[0-9]{1,3}[A-Z]?), (?<postalCode>[0-9]{4}) (?<city>[A-Za-zÆØÅæøå ]+)$");
    private final String street;
    private final String houseNumber;
    private final String postalCode;
    private final String city;
    private final OsmNode node;
    private OsmNode nearestHighway;

    public Address(OsmNode node, String bulk) {
        Matcher matcher = ADDRESS_PATTERN.matcher(bulk);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The given bulk is not a valid address");
        }

        this.node = node;
        this.street = matcher.group("street").intern();
        this.houseNumber = matcher.group("houseNumber").intern();
        this.postalCode = matcher.group("postalCode").intern();
        this.city = matcher.group("city").intern();
    }

    public Address(OsmNode node, String street, String houseNumber, String postalCode, String city) {
        this.node = node;
        this.street = street.intern();
        this.postalCode = postalCode.intern();
        this.city = city.intern();
        this.houseNumber = houseNumber.intern();
    }

    public void setNearestHighway(OsmNode nearestHighway) {
        this.nearestHighway = nearestHighway;
    }

    public OsmNode getNode() {
        return this.node;
    }

    public OsmNode getNearestHighway() {
        return this.nearestHighway;
    }

    public String getStreet() {
        return this.street;
    }

    public String getPostalCode() {
        return this.postalCode;
    }

    public String getCity() {
        return this.city;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String toShortString() {
        return String.format("%s %s %s", this.getStreet(), this.getPostalCode(), this.getCity());
    }

    @Override
    public String toString() {
        return String.format("%s %s, %s %s", this.getStreet(), this.getHouseNumber(), this.getPostalCode(), this.getCity());
    }
}

