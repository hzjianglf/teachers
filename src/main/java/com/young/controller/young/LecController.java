package com.young.controller.young;

import com.framework.util.FileUtil;
import com.young.controller.base.BaseController;
import com.young.entity.TableCopy;
import com.young.entity.User;
import com.young.json.BaseJson;
import com.young.services.le_c.impl.LecServiceImpl;
import com.young.vo.TableExtendVo;
import com.young.vo.YearVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;

@Controller
@RequestMapping(value = "/c")
public class LecController extends BaseController {
    @Resource
    private LecServiceImpl lecService;

    private BaseJson queryJson = new BaseJson();

    @RequestMapping(value = "/management", method = RequestMethod.GET)
    public ModelAndView management() throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        User user = getUserSession();
        if (user.getuType()!=3){
            modelAndView.setViewName("redirect:/c/login");
            return modelAndView;
        }
        ArrayList<User> users = lecService.get_users_by_pre_code(user.getuV2Phone());
        modelAndView.getModel().put("user",user);
        modelAndView.getModel().put("users",users);
        modelAndView.setViewName("lec/management");
        return modelAndView;
    }


    @RequestMapping(value = "/{year}/{code}/table-management", method = RequestMethod.GET)
    public ModelAndView table_management_detail(
            @PathVariable("year") String year,
            @PathVariable("code") int code
    ) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        User user = getUserSession();
        if (user.getuType()!=3){
            modelAndView.setViewName("redirect:/b/login");
            return modelAndView;
        }
        ArrayList<TableExtendVo>reList = lecService.get_tables(year,code,user);
        ArrayList<User>users = lecService.get_users_by_pre_code(user.getuV2Phone());
        ArrayList<YearVo>years = lecService.get_years();
        modelAndView.getModel().put("userself",user);
        modelAndView.getModel().put("tables",reList);
        modelAndView.getModel().put("users",users);
        modelAndView.getModel().put("years",years);
        modelAndView.getModel().put("user",code);
        modelAndView.getModel().put("year",year);
        modelAndView.setViewName("/lec/table-management");
        return modelAndView;
    }



    @RequestMapping(value = "/{year}/table-list", method = RequestMethod.GET)
    public ModelAndView table_list(
            @PathVariable("year") String year
    ) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        User user = getUserSession();
        if (user.getuType()!=3){
            modelAndView.setViewName("redirect:/b/login");
            return modelAndView;
        }
        modelAndView.getModel().put("user",user);
        ArrayList<TableCopy> tables = lecService.get_table_by_year_user(year,user);
        modelAndView.getModel().put("tables",tables);
        ArrayList<YearVo>years = lecService.get_years();
        modelAndView.getModel().put("years",years);
        modelAndView.getModel().put("year",year);
        modelAndView.setViewName("/lec/table-list");
        return modelAndView;
    }


    @RequestMapping(value = "/psd", method = RequestMethod.GET)
    public ModelAndView psd(
    ) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        User user = getUserSession();
        modelAndView.getModel().put("user",user);
        if (user.getuType()!=3){
            modelAndView.setViewName("redirect:/b/login");
            return modelAndView;
        }
        modelAndView.setViewName("/lec/psd");
        return modelAndView;
    }



    @RequestMapping(value = "/{code}/recode", method = RequestMethod.GET)
    public ModelAndView login(
            @PathVariable("code") long code
    ) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        User user = getUserSession();
        if (user.getuType()!=3){
            modelAndView.setViewName("redirect:/b/login");
            return modelAndView;
        }
        int flag = lecService.reset_pw(code);
        if(flag==1){
            modelAndView.getModel().put("msg","重置密码成功");
            modelAndView.getModel().put("url","/c/management");
        }
        else{
            modelAndView.getModel().put("msg","重置密码失败，请稍后再试");
            modelAndView.getModel().put("url","/c/management");
        }
        modelAndView.setViewName("return");
        return modelAndView;
    }



    @RequestMapping(value = "/{id}/cancel", method = RequestMethod.POST)
    public ModelAndView cancel(
            @PathVariable("id") long id,
            @RequestParam(value ="form_cancel_text", defaultValue = "") String form_cancel_text
    ) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        User user = getUserSession();
        if (user.getuType()!=3){
            modelAndView.setViewName("redirect:/b/login");
            return modelAndView;
        }
        int recode = lecService.cancel(id,form_cancel_text);
        if (recode == 1){
            modelAndView.getModel().put("msg","驳回操作成功");
            modelAndView.getModel().put("url","/c/-1/-1/table-management");
            modelAndView.setViewName("return");
        }else{
            modelAndView.getModel().put("msg","驳回操作失败");
            modelAndView.getModel().put("url","/c/-1/-1/table-management");
            modelAndView.setViewName("return");
        }
        return modelAndView;
    }



    @RequestMapping(value = "/{id}/up", method = RequestMethod.POST)
    public ModelAndView up(
            @PathVariable("id") long id,
            @RequestParam(value ="form_cancel_text", defaultValue = "") String form_cancel_text
    ) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
       User user = getUserSession();
        if (user.getuType()!=3){
            modelAndView.setViewName("redirect:/b/login");
            return modelAndView;
        }
        int recode = lecService.up(id,form_cancel_text);
        if (recode == 1){
            modelAndView.getModel().put("msg","审核通过操作成功");
            modelAndView.getModel().put("url","/c/-1/-1/table-management");
            modelAndView.setViewName("return");
        }else{
            modelAndView.getModel().put("msg","审核通过操作失败");
            modelAndView.getModel().put("url","/c/-1/-1/table-management");
            modelAndView.setViewName("return");
        }
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/{year}/downloadFile", method = RequestMethod.GET)
    private void  downloadFile3(
            @PathVariable("year") String year
    ){
        try {
            User user = getUserSession();
            ArrayList<TableCopy> tables=null;
            tables = lecService.get_table_by_year_user(year,user);
            //文件所在目录路径
            String filePath = getHttpRequest().getSession().getServletContext().getRealPath(File.separator)+File.separator+
                    "web"+File.separator+"file"+File.separator;
            String fileName = year+"年"+user.getuName()+"单位汇总表.xls";
            String path =  FileUtil.creat_file_static(year,tables,filePath,fileName,user);
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);
            //设置Http响应头告诉浏览器下载这个附件
            getHttpResponse().setHeader("Content-Disposition", "attachment;Filename=" + URLEncoder.encode(fileName, "UTF-8"));
            OutputStream outputStream = getHttpResponse().getOutputStream();
            byte[] bytes = new byte[2048];
            int len = 0;
            while ((len = fileInputStream.read(bytes))>0){
                outputStream.write(bytes,0,len);
            }
            fileInputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/change_pw", method = RequestMethod.POST)
    public ModelAndView change_pw(
            @RequestParam("old_pw") String old_pw,
            @RequestParam("new_pw1") String new_pw1,
            @RequestParam("new_pw") String new_pw,
            @RequestParam("name") String name,
            @RequestParam("phone") String phone,
            @RequestParam("mail") String mail

    ) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        if (!new_pw1.equals(new_pw)){
            modelAndView.getModel().put("msg","两次新密码必须相同");
            modelAndView.getModel().put("url","/c/psd");
            modelAndView.setViewName("return");
            return modelAndView;
        }
        User user =getUserSession();
        if (user.getuType()!=3){
            modelAndView.setViewName("redirect:/b/login");
            return modelAndView;
        }
        int flag = lecService.change_pw2(old_pw,new_pw,user.getuId(),name,phone,mail);
        if (flag==1){
            modelAndView.getModel().put("msg","修改密码成功");
            modelAndView.getModel().put("url","/b/login");
        }else if (flag==-2){
            modelAndView.getModel().put("msg","旧密码不正确");
            modelAndView.getModel().put("url","/c/psd");
        }
        else if (flag==-3){
            modelAndView.getModel().put("msg","新旧密码不能相同");
            modelAndView.getModel().put("url","/c/psd");
        }else{
            modelAndView.getModel().put("msg","修改密码失败，请联系管理员修改密码");
            modelAndView.getModel().put("url","/c/psd");
        }
        modelAndView.setViewName("return");
        return modelAndView;
    }



    @RequestMapping(value = "/{id}/up_group", method = RequestMethod.GET)
    public ModelAndView up_group(
            @PathVariable("id") String id
    ) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        User user = getUserSession();
        if (user.getuType()!=3){
            modelAndView.setViewName("redirect:/b/login");
            return modelAndView;
        }

        String[]ids = id.split("-");
        int success_num = 0;
        for (String s:ids
             ) {
            int recode = lecService.up(Long.parseLong(s),"同意");
            if (recode==1) success_num++;
        }
        modelAndView.getModel().put("msg","审核通过操作成功"+success_num+"条记录");
        modelAndView.getModel().put("url","/c/-1/-1/table-management");
        modelAndView.setViewName("return");
        return modelAndView;
    }



    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public void users() throws Exception {
        try {
            User user = getUserSession();

            ArrayList<User> users = lecService.get_users_by_pre_code(user.getuV2Phone());
            //文件所在目录路径

            String filePath = getHttpRequest().getSession().getServletContext().getRealPath(File.separator)+File.separator+
                    "web"+File.separator+"file"+File.separator;
            String fileName = user.getuName() + "单位通讯录汇总表.xls";

            String path =  FileUtil.creat_users(user,users,filePath,fileName);
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);
            //设置Http响应头告诉浏览器下载这个附件
            getHttpResponse().setHeader("Content-Disposition", "attachment;Filename=" + URLEncoder.encode(fileName, "UTF-8"));
            OutputStream outputStream = getHttpResponse().getOutputStream();
            byte[] bytes = new byte[2048];
            int len = 0;
            while ((len = fileInputStream.read(bytes))>0){
                outputStream.write(bytes,0,len);
            }
            fileInputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
