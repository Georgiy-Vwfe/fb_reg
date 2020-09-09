package com.sixhands.controller.dtos;

import com.sixhands.domain.Project;
import com.sixhands.domain.UserProjectExp;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UserProfileDTO {
    public UserProfileDTO(){}

    // ? COMMA-SEPARATED
    private List<UserProfilePropertyDTO> skills = new ArrayList<>();
    // ? COMMA-SEPARATED
    private List<UserProfilePropertyDTO> tools = new ArrayList<>();

    private List<UserProfilePropertyDTO> companies = new ArrayList<>();
    private List<UserProfilePropertyDTO> industries = new ArrayList<>();

    //not used
    private List<UserProfilePropertyDTO> getDuplicateProps(List<UserProfilePropertyDTO> profilePropDTOS){
        List<String> properties = profilePropDTOS.stream().map(UserProfilePropertyDTO::getProperty).collect(Collectors.toList());
        return profilePropDTOS.stream()
            .filter((profilePropDTO)->
                    properties.stream().filter((searchProp)->searchProp.equalsIgnoreCase(profilePropDTO.property))
                            .count() > 1
            ).collect(Collectors.toList());
    }
    public UserProfileDTO addSkill(String property, UserProjectExp projectExp, Project project){ return addPropertyFromString(property,projectExp,project,skills); }
    public UserProfileDTO addTool(String property, UserProjectExp projectExp, Project project){ return addPropertyFromString(property,projectExp,project,tools); }
    public UserProfileDTO addCompany(String property, Project project){ return addPropertyFromString(property,null,project,companies); }
    public UserProfileDTO addIndustry(String property, Project project){ return addPropertyFromString(property,null,project,industries); }
    private UserProfileDTO addPropertyFromString(String property, UserProjectExp projectExp, Project project, List<UserProfilePropertyDTO> to){
        if(projectExp == null && project == null) Logger.getGlobal().warning("Both projectExp and project is null");
        if(StringUtils.isEmpty(property) || (projectExp == null && project == null)) return this;

        //Enables splitting properties with a comma
        boolean splitMode = false;
        Arrays.stream(splitMode ? property.split(",") : new String[]{property})
                .map(String::trim)
                .filter((s)->!StringUtils.isEmpty(s)&&s.length()>1)
                .map((s)->s.substring(0,1).toUpperCase()+s.substring(1))
                .forEach((s)->addProperty(
                        project == null ?
                            new UserProfilePropertyDTO(property,projectExp) :
                            new UserProfilePropertyDTO(property,project),
                        to)
                );
        return this;
    }
    private void addProperty(UserProfilePropertyDTO prop, List<UserProfilePropertyDTO> to){
        Optional<UserProfilePropertyDTO> duplicateProp = to.stream().filter((dupProp)->dupProp.property.equalsIgnoreCase(prop.property)).findFirst();
        if(duplicateProp.isPresent()){
            duplicateProp.get().projectExps
                    .addAll(prop.getProjectExps());
            duplicateProp.get().projects
                    .addAll(prop.getProjects());
        }else to.add(prop);
    }
    //FIXME: Better way of filtering confirmed projects?
    private String propToString(List<UserProfilePropertyDTO> orig, boolean onlyConfirmed){
        //Clone property lists
        List<UserProfilePropertyDTO> propClone = new ArrayList<>(orig);
        List<List<Project>> projClones = new ArrayList<>();
        for (UserProfilePropertyDTO prop : propClone) {
            //Add project list clones to clone list
            projClones.add( new ArrayList<>(prop.getProjects()) );
            prop.setProjects(
                    prop.getProjects().stream()
                    .filter((proj)->{
                        System.out.println("proj name "+proj.getName()+" prop: "+prop.property+" is confirmed: "+proj.isConfirmed()+" onlyConfirmed: "+onlyConfirmed);
                        return proj.isConfirmed() == onlyConfirmed;
                    }).collect(Collectors.toList())
            );
        }
        String ret = propClone
                .stream()
                .filter((prop)->prop.getProjects().size()!=0)
                .map((prop)->String.format("%s(%d)", prop.property, prop.getProjects().size()))
                .collect(Collectors.joining(", "))
                .replaceAll(", $","")+"\n";
        //Reset prop projects
        for (int i = 0; i < propClone.size(); i++) propClone.get(i).setProjects(projClones.get(i));
        return ret;
    }
    @Override
    public String toString() {
        return  "Skills: "+propToString(skills,true) +
                "Tools: "+propToString(tools,true) +
                "Companies: "+propToString(companies,true) +
                "Industries: "+propToString(industries,true) +
                "==NOT CONFIRMED==\n" +
                "Skills: "+propToString(skills,false) +
                "Tools: "+propToString(tools,false) +
                "Companies: "+propToString(companies,false) +
                "Industries: "+propToString(industries,false)
                        .replace("\n","");
    }

    //#region getters/setters
    public List<UserProfilePropertyDTO> getSkills() {
        return skills;
    }

    public void setSkills(List<UserProfilePropertyDTO> skills) {
        this.skills = skills;
    }

    public List<UserProfilePropertyDTO> getCompanies() {
        return companies;
    }

    public void setCompanies(List<UserProfilePropertyDTO> companies) {
        this.companies = companies;
    }

    public List<UserProfilePropertyDTO> getIndustries() {
        return industries;
    }

    public void setIndustries(List<UserProfilePropertyDTO> industries) {
        this.industries = industries;
    }

    public List<UserProfilePropertyDTO> getTools() {
        return tools;
    }

    public void setTools(List<UserProfilePropertyDTO> tools) {
        this.tools = tools;
    }
    //#endregion

    public static class UserProfilePropertyDTO {
        public UserProfilePropertyDTO(){}
        public UserProfilePropertyDTO(String property, UserProjectExp projectExps){
            this.property = property;
            this.projectExps = new ArrayList<>( Arrays.asList(projectExps) );
        }
        public UserProfilePropertyDTO(String property, Project projects){
            this.property = property;
            this.projects = new ArrayList<>( Arrays.asList(projects) );
        }
        public UserProfilePropertyDTO(String property, UserProjectExp... projectExps){
            this.property = property;
            this.projectExps = new ArrayList<>( Arrays.asList(projectExps) );
        }
        public UserProfilePropertyDTO(String property, Project... projects){
            this.property = property;
            this.projects = new ArrayList<>( Arrays.asList(projects) );
        }

        private String property;
        //for skill, tool, industry
        private List<UserProjectExp> projectExps = new ArrayList<>();
        //for company
        private List<Project> projects = new ArrayList<>();

        //#region getters/setters
        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public List<UserProjectExp> getProjectExps() {
            return projectExps;
        }

        public void setProjectExps(List<UserProjectExp> projectExps) {
            this.projectExps = projectExps;
        }

        public List<Project> getProjects() {
            return projects;
        }

        public void setProjects(List<Project> projects) {
            this.projects = projects;
        }
        //#endregion
    }
}
