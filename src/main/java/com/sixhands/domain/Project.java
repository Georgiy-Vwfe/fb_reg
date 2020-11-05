package com.sixhands.domain;

import com.sixhands.misc.CSVSerializable;
import com.sixhands.misc.CSVMap;
import com.sixhands.misc.GenericUtils;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Entity
@Table(name = "project")
public class Project implements CSVSerializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long uuid;

    private String name;
    private String company;
    private String description;
    private String industry;
    private String start_date;
    private String end_date;
    private String link;
    @CreationTimestamp
    private Date created;
    private boolean confirmed = false;
    @ElementCollection
    @OrderColumn
    private List<Long> likedUserIDs = new ArrayList<>();
    @Column(unique = true)
    private String importID;

    public Project safeAssignProperties(Project reqProject) {
        name = reqProject.getName();
        description = reqProject.getDescription();
        company = reqProject.getCompany();
        industry = reqProject.getIndustry();
        start_date = reqProject.getStart_date();
        end_date = reqProject.getEnd_date();
        link = reqProject.getLink();
        return this;
    }

    public String getDisplayDate() {
        try {
            //TODO: Check if only start is specified, else - ret nothing
            Date start = GenericUtils.parseDateFromTHStr(start_date);
            Date end = GenericUtils.parseDateFromTHStr(end_date);
            if (start.getTime() > end.getTime())
                return null;
            Map<TimeUnit, Long> dif = GenericUtils.computeDiff(start, end);
            Long days = dif.get(TimeUnit.DAYS);
            return days + (days == 1 ? " day" : " days");
        } catch (ParseException e) {
            return null;
        }
    }

    public Project likeByUser(User user) {
        long id = user.getUuid();
        if (likedUserIDs.contains(id))
            likedUserIDs.remove(id);
        else
            likedUserIDs.add(id);
        return this;
    }

    @Override
    public Map<String, String> toCSV() {
        return new CSVMap()
                .putc("proj_id", uuid)
                .putc("proj_name", name)
                .putc("proj_desc", description)
                .putc("proj_confirmed", confirmed)
                .putc("proj_industry", industry)
                .putc("proj_start_date", start_date)
                .putc("proj_end_date", end_date)
                .putc("proj_link", link)
                .getMap();
    }

    //#region getters/setters
    public Long getUuid() {
        return uuid;
    }

    public void setUuid(Long uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<Long> getLikedUserIDs() {
        return likedUserIDs;
    }

    public void setLikedUserIDs(List<Long> likedUserIDs) {
        this.likedUserIDs = likedUserIDs;
    }

    public String getImportID() {
        return importID;
    }

    public void setImportID(String importID) {
        this.importID = importID;
    }

    //#endregion
}
