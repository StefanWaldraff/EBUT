<%@ page import="java.io.InputStream"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileNotFoundException"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>

<%
	ServletContext ctx = request.getSession().getServletContext();

	File targetFile = new File(request.getParameter("file"));
	InputStream fis = null;
	ServletOutputStream os = null;
	try {
		fis = new FileInputStream(targetFile);
		os = response.getOutputStream();
		String mimeType = ctx.getMimeType(targetFile.getAbsolutePath());
		response.setContentType(mimeType != null ? mimeType
				: "application/octet-stream");
		response.setContentLength((int) targetFile.length());
		response.setHeader("Content-Disposition",
				"attachment; filename=\"" + targetFile.getName() + "\"");

		byte[] bufferData = new byte[1024];
		int read = 0;

		while ((read = fis.read(bufferData)) != -1) {
			os.write(bufferData, 0, read);
		}
		os.flush();
		fis.close();
		os.close();

		System.out.println("File downloaded at client successfully");
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
%>