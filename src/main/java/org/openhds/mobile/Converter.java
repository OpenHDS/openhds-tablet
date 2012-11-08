package org.openhds.mobile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.Relationship;
import org.openhds.mobile.model.Round;
import org.openhds.mobile.model.SocialGroup;

import android.database.Cursor;

/**
 * Converts a cursor into a corresponding model class or a list of model class
 */
public class Converter {

    public static Individual toIndividual(Cursor cursor) {
        Individual individual = new Individual();

        if (cursor.moveToNext()) {
            populateIndividual(cursor, individual);
        }

        cursor.close();

        return individual;
    }

    private static void populateIndividual(Cursor cursor, Individual individual) {
        individual.setCurrentResidence(cursor.getString(cursor
                .getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE)));
        individual.setDob(cursor.getString(cursor.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB)));
        individual.setExtId(cursor.getString(cursor.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID)));
        individual.setFather(cursor.getString(cursor.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER)));
        individual
                .setFirstName(cursor.getString(cursor.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRSTNAME)));
        individual.setGender(cursor.getString(cursor.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER)));
        individual.setLastName(cursor.getString(cursor.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_LASTNAME)));
        individual.setMother(cursor.getString(cursor.getColumnIndex(OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER)));
    }

    public static Location toLocation(Cursor cursor) {
        Location location = new Location();

        if (cursor.moveToNext()) {
            populateLocation(cursor, location);
        }

        cursor.close();

        return location;
    }

    private static void populateLocation(Cursor cursor, Location location) {
        location.setExtId(cursor.getString(cursor.getColumnIndex(OpenHDS.Locations.COLUMN_LOCATION_EXTID)));
        location.setHierarchy(cursor.getString(cursor.getColumnIndex(OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY)));
        location.setLatitude(cursor.getString(cursor.getColumnIndex(OpenHDS.Locations.COLUMN_LOCATION_LATITUDE)));
        location.setLongitude(cursor.getString(cursor.getColumnIndex(OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE)));
        location.setName(cursor.getString(cursor.getColumnIndex(OpenHDS.Locations.COLUMN_LOCATION_NAME)));
    }

    public static LocationHierarchy toHierarhcy(Cursor cursor, boolean close) {
        LocationHierarchy hierarchy = new LocationHierarchy();

        if (cursor.moveToNext()) {
            populateHierarchy(cursor, hierarchy);
        }

        if (close) {
            cursor.close();
        }

        return hierarchy;
    }

    public static LocationHierarchy convertToHierarchy(Cursor cursor) {
        LocationHierarchy hierarchy = new LocationHierarchy();
        populateHierarchy(cursor, hierarchy);
        return hierarchy;
    }

    private static void populateHierarchy(Cursor cursor, LocationHierarchy hierarchy) {
        hierarchy.setExtId(cursor.getString(cursor.getColumnIndex(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID)));
        hierarchy.setLevel(cursor.getString(cursor.getColumnIndex(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL)));
        hierarchy.setName(cursor.getString(cursor.getColumnIndex(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME)));
        hierarchy.setParent(cursor.getString(cursor.getColumnIndex(OpenHDS.HierarchyItems.COLUMN_HIERARCHY_PARENT)));
    }

    public static FieldWorker toFieldWorker(Cursor cursor) {
        FieldWorker fw = new FieldWorker();

        if (cursor.moveToNext()) {
            fw.setExtId(cursor.getString(cursor.getColumnIndex(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_EXTID)));
            fw.setFirstName(cursor.getString(cursor.getColumnIndex(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_FIRSTNAME)));
            fw.setLastName(cursor.getString(cursor.getColumnIndex(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_LASTNAME)));
            fw.setPassword(cursor.getString(cursor.getColumnIndex(OpenHDS.FieldWorkers.COLUMN_FIELDWORKER_PASSWORD)));
        }

        cursor.close();

        return fw;
    }

    public static SocialGroup toSocialGroup(Cursor cursor) {
        SocialGroup sg = new SocialGroup();

        if (cursor.moveToNext()) {
            populateSocialGroup(cursor, sg);
        }

        cursor.close();

        return sg;
    }

    private static void populateSocialGroup(Cursor cursor, SocialGroup sg) {
        sg.setExtId(cursor.getString(cursor.getColumnIndex(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_EXTID)));
        sg.setGroupHead(cursor.getString(cursor.getColumnIndex(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPHEAD)));
        sg.setGroupName(cursor.getString(cursor.getColumnIndex(OpenHDS.SocialGroups.COLUMN_SOCIALGROUP_GROUPNAME)));
    }

    public static List<SocialGroup> toSocialGroupList(Cursor cursor) {
        List<SocialGroup> socialGroups = new ArrayList<SocialGroup>();

        while (cursor.moveToNext()) {
            SocialGroup sg = new SocialGroup();
            populateSocialGroup(cursor, sg);
            socialGroups.add(sg);
        }

        cursor.close();

        return socialGroups;
    }

    public static List<LocationHierarchy> toHierarchyList(Cursor cursor) {
        List<LocationHierarchy> hierarchys = new ArrayList<LocationHierarchy>();

        while (cursor.moveToNext()) {
            LocationHierarchy hierarchy = new LocationHierarchy();
            populateHierarchy(cursor, hierarchy);
            hierarchys.add(hierarchy);
        }

        cursor.close();

        return hierarchys;
    }

    public static List<Location> toLocationList(Cursor cursor) {
        List<Location> locations = new ArrayList<Location>();

        while (cursor.moveToNext()) {
            Location location = new Location();
            populateLocation(cursor, location);
            locations.add(location);
        }

        cursor.close();

        return locations;
    }

    public static List<Individual> toIndividualList(Cursor cursor) {
        List<Individual> individuals = new ArrayList<Individual>();

        while (cursor.moveToNext()) {
            Individual individual = new Individual();
            populateIndividual(cursor, individual);
            individuals.add(individual);
        }

        cursor.close();

        return individuals;
    }

    public static List<Round> toRoundList(Cursor cursor) {
        List<Round> rounds = new ArrayList<Round>();

        while (cursor.moveToNext()) {
            Round round = new Round();
            round.setEndDate(cursor.getString(cursor.getColumnIndex(OpenHDS.Rounds.COLUMN_ROUND_ENDDATE)));
            round.setRoundNumber(cursor.getString(cursor.getColumnIndex(OpenHDS.Rounds.COLUMN_ROUND_NUMBER)));
            round.setStartDate(cursor.getString(cursor.getColumnIndex(OpenHDS.Rounds.COLUMN_ROUND_STARTDATE)));
            rounds.add(round);
        }

        cursor.close();

        return rounds;
    }

    public static List<Relationship> toRelationshipList(Cursor cursor) {
        List<Relationship> relationships = new ArrayList<Relationship>();

        while (cursor.moveToNext()) {
            Relationship rel = new Relationship();
            rel.setIndividualA(cursor.getString(cursor
                    .getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A)));
            rel.setIndividualB(cursor.getString(cursor
                    .getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B)));
            rel.setStartDate(cursor.getString(cursor
                    .getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE)));

            relationships.add(rel);
        }

        cursor.close();

        return relationships;
    }

    public static List<Relationship> toRelationshipListSwapped(Cursor cursor) {
        List<Relationship> relationships = new ArrayList<Relationship>();

        while (cursor.moveToNext()) {
            Relationship rel = new Relationship();
            rel.setIndividualA(cursor.getString(cursor
                    .getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_B)));
            rel.setIndividualB(cursor.getString(cursor
                    .getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_INDIVIDUAL_A)));
            rel.setStartDate(cursor.getString(cursor
                    .getColumnIndex(OpenHDS.Relationships.COLUMN_RELATIONSHIP_STARTDATE)));

            relationships.add(rel);
        }

        cursor.close();

        return relationships;
    }

    public static Round convertToRound(Cursor cursor) {
        Round round = new Round();
        round.setEndDate(cursor.getString(cursor.getColumnIndex(OpenHDS.Rounds.COLUMN_ROUND_ENDDATE)));
        round.setRoundNumber(cursor.getString(cursor.getColumnIndex(OpenHDS.Rounds.COLUMN_ROUND_NUMBER)));
        round.setStartDate(cursor.getString(cursor.getColumnIndex(OpenHDS.Rounds.COLUMN_ROUND_STARTDATE)));

        return round;
    }

    public static Location convertToLocation(Cursor cursor) {
        Location location = new Location();
        populateLocation(cursor, location);

        return location;
    }

    public static Individual convertToIndividual(Cursor cursor) {
        Individual individual = new Individual();
        populateIndividual(cursor, individual);

        return individual;
    }

    public static SocialGroup convertToSocialGroup(Cursor cursor) {
        SocialGroup sg = new SocialGroup();
        populateSocialGroup(cursor, sg);
        return sg;
    }
}
