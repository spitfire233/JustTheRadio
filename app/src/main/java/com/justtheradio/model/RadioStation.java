package com.justtheradio.model;

import static com.justtheradio.util.constants.Constants.RADIO_STATIONS_DATABASE_STATION_PREFIX;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.UUID;

import de.sfuhrm.radiobrowser4j.Station;

@Entity
public class RadioStation {

    @PrimaryKey
    @NonNull
    public UUID stationUUID;
    private boolean isFavourite;
    private String state;
    private String countryCode;
    private String faviconURL;
    private String name;
    private List<String> tagsList;
    private String homePage;
    private int votes;

    public RadioStation() {

    }

    @Ignore
    public RadioStation(Station station) {
        this(station, false);
    }

    @Ignore
    public RadioStation(Station station, boolean isFavourite) {
        this(
                station.getStationUUID(),
                station.getState(),
                isFavourite,
                station.getCountryCode(),
                station.getName(),
                station.getFavicon(),
                station.getTagList(),
                station.getHomepage(),
                station.getVotes()
        );
    }

    public RadioStation(UUID stationUUID, String state, boolean isFavourite, String countryCode,
                        String name, String faviconURL, List<String> tagsList, String homePage,
                        int votes) {
        setStationUUID(stationUUID);
        setState(state);
        setFavourite(isFavourite);
        setCountryCode(countryCode);
        setName(name);
        setFaviconURL(faviconURL);
        setTagsList(tagsList);
        setHomePage(homePage);
        setVotes(votes);
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public UUID getStationUUID() {
        return stationUUID;
    }

    public void setStationUUID(UUID stationUUID) {
        this.stationUUID = stationUUID;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFaviconURL() {
        return faviconURL;
    }

    public void setFaviconURL(String faviconURL) {
        this.faviconURL = faviconURL;
    }

    public List<String> getTagsList() {
        return tagsList;
    }

    public void setTagsList(List<String> tagsList) {
        this.tagsList = tagsList;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

}
