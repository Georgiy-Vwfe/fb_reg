package com.sixhands.controller.dtos;

import com.sixhands.domain.Project;
import com.sixhands.domain.UserProjectExp;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UserProfileDTO {
    public UserProfileDTO(){}

    // COMMA-SEPARATED
    private List<UserProfilePropertyDTO> skills = new ArrayList<>();
    // COMMA-SEPARATED
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
    public UserProfileDTO addSkill(String property, UserProjectExp projectExp){ return addPropertyFromString(property,projectExp,skills); }
    public UserProfileDTO addTool(String property, UserProjectExp projectExp){ return addPropertyFromString(property,projectExp,tools); }
    public UserProfileDTO addCompany(String property, Project project){ return addPropertyFromString(property,project,companies); }
    public UserProfileDTO addIndustry(String property, Project project){ return addPropertyFromString(property,project,industries); }
    private UserProfileDTO addPropertyFromString(String property, UserProjectExp projectExp, List<UserProfilePropertyDTO> to){
        return addPropertyFromString(property,projectExp,null,to);
    }
    private UserProfileDTO addPropertyFromString(String property, Project project, List<UserProfilePropertyDTO> to){
        return addPropertyFromString(property,null,project,to);
    }
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

    @Override
    public String toString() {
        Function<List<UserProfilePropertyDTO>,String> propToString = (props)-> props.stream()
                .map((prop)->String.format("%s(%d)", prop.property, prop.getProjectExps().size()+prop.getProjects().size()))
                .collect(Collectors.joining(", "))
                .replaceAll(", $","")+"\n";
        return  "Skills: "+propToString.apply(skills) +
                "Tools: "+propToString.apply(tools) +
                "Companies: "+propToString.apply(companies) +
                "Industries: "+propToString.apply(industries)
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
