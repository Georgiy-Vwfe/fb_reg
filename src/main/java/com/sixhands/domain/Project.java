package com.sixhands.domain;

import com.sixhands.misc.GenericUtils;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Entity
@Table(name = "project")
public class Project {
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
    public Project safeAssignProperties(Project reqProject){
        name = reqProject.getName();
        description = reqProject.getDescription();
        company = reqProject.getCompany();
        industry = reqProject.getIndustry();
        start_date = reqProject.getStart_date();
        end_date = reqProject.getEnd_date();
        link = reqProject.getLink();
        return this;
    }

    public String getDuration(){
        try{
            Date start = GenericUtils.parseDateFromTHStr(start_date);
            Date end = GenericUtils.parseDateFromTHStr(end_date);
            if(start.getTime() > end.getTime())
                return null;
            Map<TimeUnit,Long> dif = GenericUtils.computeDiff(start,end);
            Long days = dif.get(TimeUnit.DAYS);
            return days+(days == 1 ? " day":" days");
        }catch(ParseException e){ return null; }
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
    //#endregion
}
