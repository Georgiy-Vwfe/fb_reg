package com.sixhands.service;

import com.sixhands.controller.dtos.ProjectDTO;
import com.sixhands.controller.dtos.UserAndExpDTO;
import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.domain.UserProjectExp;
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

    public List<ProjectDTO> parseSheet(Sheet sheet){
        User curUser = userService.getCurUserOrThrow();
        List<ProjectDTO> sheetProjectDTOs = new ArrayList<>();
        ProjectDTOBuilder projectDTOBuilder = new ProjectDTOBuilder();

        int r = 0;
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()){
            Row row = rowIterator.next();
            if(r==0){ r++; continue; }
            if (row != null) {
                int c = Math.max(0, row.getFirstCellNum()-1);
                Iterator<Cell> cellIterator = row.iterator();
                while (cellIterator.hasNext()){
                    Cell cell = cellIterator.next();
                    if(cell != null){
                        Supplier<String> cs = () -> dataFormatter.formatCellValue(cell);
                        if( !StringUtils.isEmpty( cs.get() ) ){
                            //System.out.printf("row: %d, col: %d, %s%n", r, c, cs.get());
                            switch (c){
                                case 0: projectDTOBuilder.setName(cs.get()); break;
                                case 1: projectDTOBuilder.setDescription(cs.get()); break;
                                case 2: projectDTOBuilder.setCompany(cs.get()); break;
                                case 3: projectDTOBuilder.setIndustry(cs.get()); break;
                                //TODO: Validate dates
                                case 4: projectDTOBuilder.setStartDate(cs.get()); break;
                                case 5: projectDTOBuilder.setEndDate(cs.get()); break;

                                case 6: projectDTOBuilder.setMemberName(cs.get()); break;
                                case 7: projectDTOBuilder.setMemberSurname(cs.get()); break;
                                case 8: projectDTOBuilder.setMemberEmail(cs.get()); break;
                                case 9: projectDTOBuilder.setMemberRole(cs.get()); break;
                            }
                        }
                    }
                    c++;
                }
            }
            Cell nextRowFirstCell = null, nextRowMemberCell = null;
            try{ nextRowFirstCell = sheet.getRow(r+1).getCell(0); nextRowMemberCell = sheet.getRow(r+1).getCell(7); }
            catch (Exception ignored){}
            boolean nextIsNewProject = nextRowFirstCell!=null && nextRowMemberCell!=null && !StringUtils.isEmpty(nextRowFirstCell.getStringCellValue()) && !StringUtils.isEmpty(nextRowMemberCell.getStringCellValue());
            //System.out.printf("r: %d total-1: %d%n", r, sheet.getPhysicalNumberOfRows() - 1);
            if( nextIsNewProject || r == sheet.getPhysicalNumberOfRows()-1 ){
                ProjectDTO projectDTO = projectDTOBuilder.build();
                projectDTOBuilder = new ProjectDTOBuilder();
                sheetProjectDTOs.add(projectDTO);
            }

            r++;
        }

        return sheetProjectDTOs;
    }

    private static class ProjectDTOBuilder {
        private UserAndExpDTO tempUserAndExpDTO = new UserAndExpDTO();

        private ProjectDTO projectDTO = new ProjectDTO(); private Project proj(){ return projectDTO.getProject(); }

        //#region Project
        public ProjectDTOBuilder setName(String name){ proj().setName(name); return this;}
        public ProjectDTOBuilder setDescription(String description){ proj().setDescription(description); return this;}
        public ProjectDTOBuilder setCompany(String company){ proj().setCompany(company); return this;}
        public ProjectDTOBuilder setIndustry(String industry){ proj().setIndustry(industry); return this;}
        public ProjectDTOBuilder setStartDate(String startDate){ proj().setStart_date(startDate); return this;}
        public ProjectDTOBuilder setEndDate(String endDate){ proj().setStart_date(endDate); return this;}
        //#endregion

        //#region Members
        public ProjectDTOBuilder setMemberName(String name){
            tempUserAndExpDTO.getUser().setFirst_name(name);
            afterTempUserAndExpChange();
            return this;
        }
        public ProjectDTOBuilder setMemberSurname(String surname){
            tempUserAndExpDTO.getUser().setLast_name(surname);
            afterTempUserAndExpChange();
            return this;
        }
        public ProjectDTOBuilder setMemberEmail(String email){
            tempUserAndExpDTO.getUser().setEmail(email);
            afterTempUserAndExpChange();
            return this;
        }
        public ProjectDTOBuilder setMemberRole(String role){
            tempUserAndExpDTO.getUserExp().setRole(role);
            afterTempUserAndExpChange();
            return this;
        }
        private void afterTempUserAndExpChange(){
            User user = tempUserAndExpDTO.getUser();
            UserProjectExp exp = tempUserAndExpDTO.getUserExp();


            boolean swapTempUserAndExp = !StringUtils.isEmpty(user.getFirst_name()) &&
                    !StringUtils.isEmpty(user.getLast_name()) &&
                    !StringUtils.isEmpty(user.getEmail()) &&
                    !StringUtils.isEmpty(exp.getRole());
            //System.out.printf("fn: %s, sn: %s, mail: %s, role: %s, swap: %s\n",user.getFirst_name(),user.getLast_name(),user.getEmail(),exp.getRole(),swapTempUserAndExp);

            if(!swapTempUserAndExp) return;

            List<UserAndExpDTO> members = new ArrayList<>( Arrays.asList( projectDTO.getMembers() ) );
            int memI = (int) members.stream().filter(Objects::nonNull).count();
            if(memI >= projectDTO.getMembers().length-1) return;

            projectDTO.getMembers()[memI] = tempUserAndExpDTO;
            tempUserAndExpDTO = new UserAndExpDTO();
        }
        //#endregion
        public ProjectDTO build(){
            projectDTO.getMember().getUserExp().setProject_creator(true);
            return projectDTO;
        }
    }
}
