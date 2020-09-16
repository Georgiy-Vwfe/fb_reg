package com.sixhands.domain;

import com.sixhands.misc.CSVMap;
import com.sixhands.misc.CSVSerializable;
import com.sixhands.misc.GenericUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

@Entity
@Table(name = "user")
//Ignore properties on deserialization
//@JsonIgnoreProperties(value={ "uuid", "role", "activationCode", "create_time", "rating" }, allowGetters=true)
public class User implements UserDetails, CSVSerializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long uuid;

    @NotNull
    private String email;

    @NotNull
    @Size(min = 3, message = "Password can have 3+ symbols")
    private String password;
    @Transient
    private String confirmPassword;

    private String first_name;
    private String last_name;
    private char sex;
    private String country;
    private String about_user;
    private Integer rating;
    private String date_of_birth;
    private String phone_number;
    private String city;
    private String user_img;
    private String social_networks;
    @CreationTimestamp
    private Date creation_timestamp;
    private String role;

    //Set to true when user confirms a project for the first time
    //Used when calculating user rating
    private boolean confirmed_project;

    private String activationCode;

    @Override
    public Map<String, String> toCSV() {
        return new CSVMap()
                .putc("user_id",uuid)
                .putc("user_email",email)
                .putc("user_verified",activationCode==null)
                .putc("user_first_name",first_name)
                .putc("user_last_name",last_name)
                .putc("user_sex",sex)
                .putc("user_date_of_birth",date_of_birth)
                .putc("user_about_me",about_user)
                .putc("user_registration_date",creation_timestamp == null ? "null" : GenericUtils.formatDateToTHStr(creation_timestamp))
                .putc("user_country",country)
                .putc("user_city",city);
    }

    public User safeAssignProperties(User editUser) {
        country = editUser.getCountry();
        city = editUser.getCity();

        date_of_birth = editUser.getDate_of_birth();

        first_name = editUser.getFirst_name();
        last_name = editUser.getLast_name();

        social_networks = editUser.getSocial_networks();
        user_img = editUser.getUser_img();
        about_user = editUser.getAbout_user();

        email = editUser.getEmail();
        phone_number = editUser.getPhone_number();
        //TODO: Parse&validate date
        date_of_birth = editUser.getDate_of_birth();

        sex = editUser.getSex();

        return this;
    }

    //TODO: Return stored authorities ?as list
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new HashSet<>(AuthorityUtils.createAuthorityList(role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activationCode==null;
    }

    //#region getters/setters

    public Long getUuid() {
        return uuid;
    }

    public void setUuid(Long uuid) {
        this.uuid = uuid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public char getSex() {
        return sex;
    }

    public void setSex(char sex) {
        this.sex = sex;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAbout_user() {
        return about_user;
    }

    public void setAbout_user(String about_user) {
        this.about_user = about_user;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUser_img() {
        return user_img;
    }

    public void setUser_img(String user_img) {
        this.user_img = user_img;
    }

    public String getSocial_networks() {
        return social_networks;
    }

    public void setSocial_networks(String social_networks) {
        this.social_networks = social_networks;
    }

    public Date getCreation_timestamp() {
        return creation_timestamp;
    }

    public void setCreate_time(Date create_time) {
        this.creation_timestamp = create_time;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public boolean getConfirmed_project() {
        return confirmed_project;
    }

    public void setConfirmed_project(boolean confirmed_project) {
        this.confirmed_project = confirmed_project;
    }
    //#endregion
}
