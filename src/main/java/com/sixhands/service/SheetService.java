package com.sixhands.service;

import com.sixhands.controller.dtos.ProjectDTO;
import com.sixhands.controller.dtos.UserAndExpDTO;
import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.misc.GenericUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Supplier;

@Service
public class SheetService {

    @Autowired
    private UserService userService;
    private static DataFormatter dataFormatter = new DataFormatter();
    private int totalCells=-1;
    //Cell, where member first name is set
    private final int MEMBERS_CELL = 9;
    private int c;
    private Row row;
    public List<ProjectDTO> parseSheet(Sheet sheet){
        User curUser = userService.getCurUserOrThrow();
        List<ProjectDTO> sheetProjectDTOs = new ArrayList<>();
        ProjectDTOBuilder projectDTOBuilder = new ProjectDTOBuilder();

        int r = 0;
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()){
            row = rowIterator.next();
            if(r ==0){
                r++;
                projectDTOBuilder = new ProjectDTOBuilder();
                continue;
            }
            if (row != null) {
                c = Math.max(0, row.getFirstCellNum()-1);
                if(totalCells == -1) totalCells = row.getPhysicalNumberOfCells();
                Iterator<Cell> cellIterator = row.iterator();
                while (cellIterator.hasNext()){
                    Cell cell = cellIterator.next();
                    if(cell != null){
                        Supplier<String> cs = () -> dataFormatter.formatCellValue(cell);
                        if( !StringUtils.isEmpty( cs.get() ) ){
                            //System.out.printf("row: %d, col: %d, %s%n", r, c, cs.get());
                            switch (c){
                                case 0: projectDTOBuilder.setProjectName(cs.get()); break;
                                case 1: projectDTOBuilder.setProjectDescription(cs.get()); break;
                                case 2: projectDTOBuilder.setProjectCompany(cs.get()); break;
                                case 3: projectDTOBuilder.setProjectIndustry(cs.get()); break;
                                case 4: projectDTOBuilder.setProjectStartDate(cs.get()); break;
                                case 5: projectDTOBuilder.setProjectEndDate(cs.get()); break;
                                case 6: projectDTOBuilder.setProjectLink(cs.get()); break;
                                case 7: projectDTOBuilder.setProjectImportID(cs.get()); break;
                                case 8: projectDTOBuilder.setUserName(cs.get()); break;
                                case 9: projectDTOBuilder.setUserSurname(cs.get()); break;
                                case 10: projectDTOBuilder.setUserEmail(cs.get()); break;
                                case 11: projectDTOBuilder.setMemberRole(cs.get()); break;
                                case 12: projectDTOBuilder.setMemberPosition(cs.get());  break;
                                case 13: projectDTOBuilder.setMemberDuties(cs.get());  break;
                                case 14: projectDTOBuilder.setMemberSkills(cs.get());  break;
                                case 15: projectDTOBuilder.setMemberTools(cs.get());  break;
                                case 16: projectDTOBuilder.setUserCountry(cs.get());  break;
                                case 17: projectDTOBuilder.setUserCity(cs.get());  break;
                                case 18: projectDTOBuilder.setUserDateOfBirth(cs.get());  break;
                                case 19: projectDTOBuilder.setUserSex(cs.get());  break;
                                case 20: projectDTOBuilder.setUserPhoneNumber(cs.get()); break;
                                case 21: projectDTOBuilder.setUserAboutMe(cs.get()); break;
                                case 22: projectDTOBuilder.setUserSocialNetworks(cs.get()); break;
                            }
                        }
                    }
                    c++;
                }
            }
            Cell nextRowFirstCell = null, nextRowMemberCell = null;
            try{ nextRowFirstCell = sheet.getRow(r +1).getCell(0); nextRowMemberCell = sheet.getRow(r + 1).getCell(MEMBERS_CELL); }
            catch (Exception ignored){}
            boolean nextIsNewProject = nextRowFirstCell!=null &&
                    nextRowMemberCell!=null &&
                    !StringUtils.isEmpty(nextRowFirstCell.getStringCellValue()) &&
                    !StringUtils.isEmpty(nextRowMemberCell.getStringCellValue());
            //System.out.printf("r: %s total-1: %s, nextIsNewProject: %s\n", r, (sheet.getPhysicalNumberOfRows() - 1), nextIsNewProject);
            if( nextIsNewProject || r == sheet.getPhysicalNumberOfRows()-1 ){
                ProjectDTO projectDTO = projectDTOBuilder.build();
                r++;
                projectDTOBuilder = new ProjectDTOBuilder();
                sheetProjectDTOs.add(projectDTO);
            }else r++;

        }

        return sheetProjectDTOs;
    }
    //TODO: Refactor afterTempUserAndExpChange
    private class ProjectDTOBuilder {
        private UserAndExpDTO tempUserAndExpDTO = new UserAndExpDTO();

        private ProjectDTO projectDTO = new ProjectDTO(); private Project proj(){ return projectDTO.getProject(); }

        //#region Project
        public ProjectDTOBuilder setProjectName(String name){ proj().setName(name); return this;}
        public ProjectDTOBuilder setProjectDescription(String description){ proj().setDescription(description); return this;}
        public ProjectDTOBuilder setProjectCompany(String company){ proj().setCompany(company); return this;}
        public ProjectDTOBuilder setProjectIndustry(String industry){ proj().setIndustry(industry); return this;}
        public ProjectDTOBuilder setProjectImportID(String s) { proj().setImportID(s); return this; }
        public ProjectDTOBuilder setProjectStartDate(String startDate){
            if(!GenericUtils.isDateFormattedAsTHStr(startDate)) return this;
            proj().setStart_date(startDate);
            return this;
        }
        public ProjectDTOBuilder setProjectEndDate(String endDate){
            if(!GenericUtils.isDateFormattedAsTHStr(endDate)) return this;
            proj().setEnd_date(endDate);
            return this;
        }
        public ProjectDTOBuilder setProjectLink(String link){ proj().setLink(link); return this;}

        //#endregion
        //#region UserExp
        public ProjectDTOBuilder setMemberRole(String role){
            tempUserAndExpDTO.getUserExp().setRole(role);
            return afterTempUserAndExpChange();
        }
        public ProjectDTOBuilder setMemberPosition(String position){
            tempUserAndExpDTO.getUserExp().setPosition(position);
            return afterTempUserAndExpChange();
        }
        public ProjectDTOBuilder setMemberDuties(String duties){
            tempUserAndExpDTO.getUserExp().setDuties(duties);
            return afterTempUserAndExpChange();
        }
        public ProjectDTOBuilder setMemberSkills(String skills){
            tempUserAndExpDTO.getUserExp().setSkills(skills);
            return afterTempUserAndExpChange();
        }
        public ProjectDTOBuilder setMemberTools(String tools){
            tempUserAndExpDTO.getUserExp().setTools(tools);
            return afterTempUserAndExpChange();
        }
        //#endregion
        //#region User
        public ProjectDTOBuilder setUserDateOfBirth(String dateOfBirth){
            if(!GenericUtils.isDateFormattedAsTHStr(dateOfBirth)) return this;
            tempUserAndExpDTO.getUser().setDate_of_birth(dateOfBirth);
            return afterTempUserAndExpChange();
        }
        public ProjectDTOBuilder setUserCity(String city){
            tempUserAndExpDTO.getUser().setCity(city);
            return afterTempUserAndExpChange();
        }
        public ProjectDTOBuilder setUserCountry(String country){
            tempUserAndExpDTO.getUser().setCountry(country);
            return afterTempUserAndExpChange();
        }
        public ProjectDTOBuilder setUserSex(String strSex){
            if(StringUtils.isEmpty(strSex)) return this;
            char sex = strSex.toUpperCase().charAt(0);
            if(sex!='M'&&sex!='F') return this;
            tempUserAndExpDTO.getUser().setSex(sex);
            return afterTempUserAndExpChange();
        }
        public ProjectDTOBuilder setUserAboutMe(String aboutMe){
            tempUserAndExpDTO.getUser().setAbout_user(aboutMe);
            return afterTempUserAndExpChange();
        }
        public ProjectDTOBuilder setUserPhoneNumber(String phoneNumber){
            tempUserAndExpDTO.getUser().setPhone_number(phoneNumber);
            return afterTempUserAndExpChange();
        }
        public ProjectDTOBuilder setUserSocialNetworks(String socialNetworks){
            tempUserAndExpDTO.getUser().setSocial_networks(socialNetworks);
            return afterTempUserAndExpChange();
        }
        public ProjectDTOBuilder setUserName(String name){
            tempUserAndExpDTO.getUser().setFirst_name(name);
            return afterTempUserAndExpChange();
        }
        public ProjectDTOBuilder setUserSurname(String surname){
            tempUserAndExpDTO.getUser().setLast_name(surname);
            return afterTempUserAndExpChange();
        }
        public ProjectDTOBuilder setUserEmail(String email){
            tempUserAndExpDTO.getUser().setEmail(email);
            return afterTempUserAndExpChange();
        }
        //#endregion

        private ProjectDTOBuilder afterTempUserAndExpChange(){
            //System.out.printf("row: %d, prevRow: %d, user: %s\n", r, prevRow, (tempUserAndExpDTO.getUser().getFirst_name()+tempUserAndExpDTO.getUser().getLast_name()));
            //System.out.println(new JSONObject(projectDTO).toString(2));
            if(c!=totalCells-1) return this;

            List<UserAndExpDTO> members = new ArrayList<>( Arrays.asList( projectDTO.getMembers() ) );
            int memI = (int) members.stream().filter(Objects::nonNull).count();
            if(memI >= projectDTO.getMembers().length-1) return this;

            projectDTO.getMembers()[memI] = tempUserAndExpDTO;
            //System.out.println("Setting new user to index "+memI);
            tempUserAndExpDTO = new UserAndExpDTO();
            return this;
        }
        public ProjectDTO build(){
            projectDTO.getMember().getUserExp().setProject_creator(true);
            return projectDTO;
        }
    }
}
