package org.mfr.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.mfr.data.Constants;
import org.mfr.image.ImageTools;
import org.mfr.web.action.ImageHandlerComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.spring.SpringUtil;

public class UploadServlet extends HttpServlet {
	public static final String UPLOADEDFILESSESSIONATTR="UploadedFiles";
	private static final Logger logger=LoggerFactory.getLogger(UploadServlet.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		///req.getSession().getAttribute("user")
		/// session authentication must be here
		String userSession=null;
		OutputStreamWriter os=new OutputStreamWriter(resp.getOutputStream());
		if(ServletFileUpload.isMultipartContent(req)){
			logger.info("FileUpload called");	
			String tmpdir=Constants.TEMPDIR+"/work/";
			File tempD=new File(tmpdir);
			tempD.mkdir();
			ServletFileUpload sfu=new ServletFileUpload(new DiskFileItemFactory(100, tempD));
			
			try {
				List<FileItem> files=sfu.parseRequest(req);
				List<UploadedFileDescriptor> uploadedFiles=new ArrayList<UploadedFileDescriptor>();
				if(files!=null && files.size()>0){
					os.write("{\"files\": [");
					int count=0;
					for (FileItem  fi: files) {
						if(count!=0){
							os.write(",");
						}

						if(!fi.isFormField()){
							UploadedFileDescriptor uploadFileDesc=new UploadedFileDescriptor(fi.getName(), fi.getInputStream());
							uploadedFiles.add(uploadFileDesc);
							os.write("{"+
						             "\"name\":\""+fi.getName()+"\","+
						             "\"size\": "+fi.getSize()+
						             "}");
							count++;
						}else if(fi.getFieldName()!=null && fi.getFieldName().equals("session")){ // session ID
							userSession=fi.getString();
						}
						
					}
					os.write("]}");
					extractFilesToTemp(userSession, uploadedFiles);
					
				}else{
					logger.error("no file");
					os.write("ERROR:no file");
				}
			} catch (FileUploadException e) {
				os.write("ERROR:"+e.getMessage());
				logger.error("file upload error", e);
			} catch (Exception e){
				logger.error("file upload error", e);
			}
		}else{
			os.write("ERROR:FileUpload called no multipart");
			logger.info("FileUpload called no multipart");
		}
		os.flush();
		os.close();
		
	}

	public static void extractFilesToTemp(String userSession,
			List<UploadedFileDescriptor> uploadedFiles)
			throws FileNotFoundException, IOException {
		String targetDirectory=Constants.TEMPDIR+"/"+userSession+"/";
		logger.debug("targetDirectory ["+targetDirectory+"]");
		File targetdir=new File(targetDirectory);
		if(!targetdir.exists() && !targetdir.mkdir()){
			logger.error("CREATE TARGETDIRECTORY ERROR");
		}
		for (int i = 0; i < uploadedFiles.size(); i++) {
			String filenameTmp=uploadedFiles.get(i).getName();
			
			String filename = ImageTools.calcNewFileName(targetDirectory, filenameTmp);
			logger.info(uploadedFiles.get(i).getName()+" rename to ["+filename+"]");
			String newFileName=Constants.TEMPDIR+"/"+userSession+"/"+filename;
			logger.debug("newFileName ["+newFileName+"]");
			uploadedFiles.get(i).setFilePath(newFileName);
			uploadedFiles.get(i).setName(filename);
			FileOutputStream fos=new FileOutputStream(newFileName);
			byte [] buffer=new byte[10000];
			int count=0;
			InputStream is=uploadedFiles.get(i).getInputStream();
			while ((count=is.read(buffer))>-1) {
				fos.write(buffer,0,count);
			}
			fos.close();
			is.close();
		}
		logger.info("uploaded files count ["+uploadedFiles.size()+"]");				
		
	}

	

}
