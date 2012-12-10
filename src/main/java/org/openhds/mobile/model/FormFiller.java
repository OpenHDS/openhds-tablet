package org.openhds.mobile.model;

/**
 * Fills in the event forms with pre-filled values based on the location visit
 * and possibly other entity types
 */
public class FormFiller {

    public FilledForm fillLocationForm(LocationVisit locationVisit) {
        FilledForm form = new FilledForm(UpdateEvent.LOCATION);

        addFieldWorker(locationVisit, form);

        form.setLocationId(locationVisit.getLocation().getExtId());
        form.setHierarchyId(locationVisit.getHierarchy4().getExtId());

        return form;
    }

    private void addFieldWorker(LocationVisit locationVisit, FilledForm form) {
        form.setFieldWorkerId(locationVisit.getFieldWorker().getExtId());
    }

    public FilledForm fillVisitForm(LocationVisit locationVisit) {
        FilledForm form = new FilledForm(UpdateEvent.VISIT);

        addFieldWorker(locationVisit, form);
        addVisit(locationVisit, form);

        form.setLocationId(locationVisit.getLocation().getExtId());
        form.setRoundNumber(locationVisit.getRound().getRoundNumber());

        return form;
    }

    private void addVisit(LocationVisit locationVisit, FilledForm form) {
        form.setVisitDate(locationVisit.getVisit().getDate());
        form.setVisitExtId(locationVisit.getVisit().getExtId());
    }

    public FilledForm fillSocialGroupForm(LocationVisit locationVisit, SocialGroup sg) {
        FilledForm form = new FilledForm(UpdateEvent.SOCIALGROUP);

        addFieldWorker(locationVisit, form);
        addVisit(locationVisit, form);
        addHousehold(sg, form);

        form.setIndividualExtId(locationVisit.getSelectedIndividual().getExtId());

        return form;
    }

    private void addHousehold(SocialGroup sg, FilledForm form) {
        form.setHouseholdId(sg.getExtId());
        form.setHouseholdName(sg.getGroupName());
    }

    public FilledForm fillMembershipForm(LocationVisit locationVisit) {
        FilledForm form = new FilledForm(UpdateEvent.MEMBERSHIP);

        addFieldWorker(locationVisit, form);

        form.setIndividualExtId(locationVisit.getSelectedIndividual().getExtId());
        form.setIndividualFirstName(locationVisit.getSelectedIndividual().getFirstName());
        form.setIndividualFirstName(locationVisit.getSelectedIndividual().getLastName());

        return form;
    }

    public FilledForm appendSocialGroup(SocialGroup sg, FilledForm form) {
        addHousehold(sg, form);

        return form;
    }

    public FilledForm fillRelationships(LocationVisit locationVisit) {
        FilledForm form = new FilledForm(UpdateEvent.RELATIONSHIP);

        addFieldWorker(locationVisit, form);
        Individual indiv = locationVisit.getSelectedIndividual();
        form.setIndividualA(indiv.getExtId());

        return form;
    }

    public FilledForm appendIndividual(Individual individual, FilledForm form) {
        addIndividual(individual, form);

        return form;
    }

    private void addIndividual(Individual individual, FilledForm form) {
        form.setIndividualDob(individual.getDob());
        form.setIndividualExtId(individual.getExtId());
        form.setIndividualFirstName(individual.getFirstName());
        form.setIndividualGender(individual.getGender());
        form.setIndividualLastName(individual.getLastName());
    }

    public FilledForm fillOutMigrationForm(LocationVisit locationVisit) {
        FilledForm form = new FilledForm(UpdateEvent.OUTMIGRATION);

        addFieldWorker(locationVisit, form);
        addVisit(locationVisit, form);
        addIndividual(locationVisit.getSelectedIndividual(), form);

        return form;
    }

    public FilledForm fillPregnancyRegistrationForm(LocationVisit locationVisit) {
        FilledForm form = new FilledForm(UpdateEvent.PREGNANCYOBSERVATION);

        addFieldWorker(locationVisit, form);
        addVisit(locationVisit, form);
        addIndividual(locationVisit.getSelectedIndividual(), form);

        return form;
    }

    public FilledForm fillDeathForm(LocationVisit locationVisit) {
        FilledForm form = new FilledForm(UpdateEvent.DEATH);

        addFieldWorker(locationVisit, form);
        addVisit(locationVisit, form);
        addIndividual(locationVisit.getSelectedIndividual(), form);

        return form;
    }

    public FilledForm fillInMigrationForm(LocationVisit locationVisit, Individual individual) {
        FilledForm form = new FilledForm(UpdateEvent.INMIGRATION);

        addFieldWorker(locationVisit, form);
        addVisit(locationVisit, form);
        form.setLocationId(locationVisit.getLocation().getExtId());
        if (individual != null) {
            addIndividual(individual, form);
        }

        return form;
    }

    public FilledForm fillPregnancyOutcome(LocationVisit locationVisit, PregnancyOutcome po) {
        FilledForm form = new FilledForm(UpdateEvent.PREGNANCYOUTCOME);

        addFieldWorker(locationVisit, form);
        addVisit(locationVisit, form);
        form.setLocationId(locationVisit.getLocation().getExtId());
        form.setMotherExtId(locationVisit.getSelectedIndividual().getExtId());
        
        for(String childId : po.getChildIds()) {
            Child child = new Child();
            child.setId(childId);
            form.addChild(child);
        }

        return form;
    }

    public FilledForm fillInternalInMigrationForm(LocationVisit locationVisit, Individual individual) {
        FilledForm form = new FilledForm(UpdateEvent.INMIGRATION);

        addFieldWorker(locationVisit, form);
        addVisit(locationVisit, form);

        if (individual != null) {
            addIndividual(individual, form);
        }

        form.setMigrationType("INTERNAL_INMIGRATION");
        form.setLocationId(locationVisit.getLocation().getExtId());

        return form;
    }

    public FilledForm fillExternalInmigration(LocationVisit locationVisit, String id) {
        FilledForm form = new FilledForm(UpdateEvent.INMIGRATION);

        addFieldWorker(locationVisit, form);
        addVisit(locationVisit, form);

        form.setIndividualExtId(id);
        form.setMigrationType("INTERNAL_INMIGRATION");
        form.setLocationId(locationVisit.getLocation().getExtId());
        form.setMigrationType("EXTERNAL_INMIGRATION");
        
        return form;
    }

    public void appendFatherId(FilledForm filledForm, String fatherId) {
        filledForm.setFatherExtId(fatherId);
    }
}
