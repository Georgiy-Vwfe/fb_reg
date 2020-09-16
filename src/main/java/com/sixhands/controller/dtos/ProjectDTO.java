package com.sixhands.controller.dtos;

import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.domain.UserProjectExp;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ProjectDTO {
    private Project project = new Project();
    private UserAndExpDTO member = new UserAndExpDTO();
    private UserAndExpDTO[] members = new UserAndExpDTO[10];

    public CSVProjectDTO toCsvDto(){
        Map<String, String> projectMap = project.toCSV();
        List<UserAndExpDTO> membersList = new ArrayList<>(Arrays.asList( members ));
        if(member!=null&&!StringUtils.isEmpty(member.getUser().getEmail())) membersList.add(member);
        List<Map<String, String>> memberMaps = membersList.stream()
                .filter(Objects::nonNull)
                .map(UserAndExpDTO::toCSV)
                .collect(Collectors.toList());

        return new CSVProjectDTO(projectMap,memberMaps);
    }

    public void addNewMember(){
        int index = Arrays.asList(members).indexOf(null);
        if(index == -1) return;
        members[index] = new UserAndExpDTO(new User(), new UserProjectExp());
        members[index].setAdded(true);
    }

    public void deleteMember(int index){
        members[index] = null;
    }
    //#region getters/setters

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public UserAndExpDTO[] getMembers(){
        return members;
    }

    public void setMembers(UserAndExpDTO[] members){
        this.members = members;
    }

    public UserAndExpDTO getMember() {
        return member;
    }

    public void setMember(UserAndExpDTO projectExp) {
        this.member = projectExp;
    }
    //#endregion

    public static class CSVProjectDTO{
        public CSVProjectDTO(Map<String, String> project, List<Map<String, String>> members) {
            this.project = project;
            this.members = members;
        }

        private Map<String, String> project;
        private List<Map<String, String>> members;

        public Map<String, String> getProject() {
            return project;
        }

        public List<Map<String, String>> getMembers() {
            return members;
        }

        @Override
        public String toString(){
            return toString(true);
        }
        public String toString(boolean addTitles) {
            List<String> rows = new ArrayList<>();
            String titles = String.join(",", project.keySet())+","+String.join(",",members.get(0).keySet());
            String firstRow = String.join(",", project.values())+","+String.join(",",members.get(0).values());
            if(addTitles) rows.add(titles);
            rows.add(firstRow);
            members.subList(1,members.size())
                    .forEach((m)->{
                        String projSkip = project.values().stream().map((p)->",").collect(Collectors.joining());
                        String memValues = String.join(",", m.values() );
                        rows.add(projSkip + memValues);
                    });
            return String.join("\n", rows);
        }
    }
}
