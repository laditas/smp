package com.alipapa.smp.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel导出
 */
public class ExcelExport {

    private static Logger logger = LoggerFactory.getLogger(ExcelRead.class);

    /**
     * 生成excel并下载
     */
    public static void exportExcel(String longString, List<ArrayList<String>> failedList, HttpServletResponse response, String pathPrefix) {

        //String basePath = Thread.currentThread().getContextClassLoader().getResource("").toString() + File.separator + "template";


        String basePath = "/Users/maibahe/IdeaProjects/smp_frontier/target/classes/template";
        File newFile = createNewFile(basePath, pathPrefix);

        //******************新文件写入数据，并下载************************************
        InputStream is = null;
        //HSSFWorkbook workbook = null;
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = null;
        try {
            is = new FileInputStream(newFile);
            workbook = new XSSFWorkbook(is);
            //获取第一个sheet
            sheet = workbook.getSheetAt(0);
        } catch (Exception e1) {
            logger.error("", e1);
        }

        if (sheet != null) {
            try {
                //写数据
                FileOutputStream fos = new FileOutputStream(newFile);

                //第一行，上传混总
                XSSFRow row = sheet.getRow(0);
                XSSFCell cell = row.getCell(1);
                cell.setCellValue(longString);

                if (!CollectionUtils.isEmpty(failedList)) {
                    //第二行是表头,从第三行开始写数据
                    for (int i = 0; i < failedList.size(); i++) {
                        ArrayList<String> consumerRowList = failedList.get(i);
                        XSSFRow nowRow = sheet.createRow(2 + i);
                        for (int j = 0; j < consumerRowList.size(); j++) {
                            XSSFCell nowCell = nowRow.createCell(j);
                            nowCell.setCellValue(consumerRowList.get(j));
                        }
                    }
                }
                workbook.write(fos);
                fos.flush();
                fos.close();

                //下载
                InputStream fis = new BufferedInputStream(new FileInputStream(newFile));
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                fis.close();
                response.reset();
                response.setContentType("text/html;charset=UTF-8");
                OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
                response.setContentType("application/x-msdownload");
                String newName = URLEncoder.encode("导入结果" + System.currentTimeMillis() + ".xlsx", "UTF-8");
                response.addHeader("Content-Disposition", "attachment;filename=\"" + newName + "\"");
                response.addHeader("Content-Length", "" + newFile.length());
                toClient.write(buffer);
                toClient.flush();
            } catch (Exception e) {
                logger.error("生成excel并下载异常", e);
            } finally {
                try {
                    if (null != is) {
                        is.close();
                    }
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        }
        //删除创建的新文件
        //deleteFile(newFile);
    }

    /**
     * 复制文件
     *
     * @param s 源文件
     * @param t 复制到的新文件
     */

    public static void fileChannelCopy(File s, File t) {
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new BufferedInputStream(new FileInputStream(s), 1024);
                out = new BufferedOutputStream(new FileOutputStream(t), 1024);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            } finally {
                if (null != in) {
                    in.close();
                }
                if (null != out) {
                    out.close();
                }
            }
        } catch (Exception e) {
            logger.error("复制到新文件异常", e);
        }
    }

    /**
     * 读取excel模板，并复制到新文件中供写入和下载
     *
     * @param templatePath //模板文件的路径
     * @param realPath     //保存文件的路径
     * @return
     */
    public static File createNewFile(String templatePath, String realPath) {
        //***********************************读取模板，并赋值到新文件*************************
        //文件模板路径
        String path = templatePath;
        String fileName = "Consumer_DownLoad_Template.xlsx";
        File file = new File(path + File.separator + fileName);


        //新的文件名
        String newFileName = "导入结果" + System.currentTimeMillis() + "." + ExcelUtil.OFFICE_EXCEL_2010_POSTFIX;
        //判断路径是否存在
        File dir = new File(realPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //写入到新的excel
        File newFile = new File(realPath, newFileName);
        try {
            newFile.createNewFile();
            //复制模板到新文件
            fileChannelCopy(file, newFile);
        } catch (Exception e) {
            logger.error("读取excel模板异常", e);
        }
        return newFile;
    }

    /**
     * 下载成功后删除
     *
     * @param files
     */
    private static void deleteFile(File... files) {
        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
