package tools;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;

public class SPARQL extends HttpServlet {

	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// http://de.slideshare.net/olafhartig/querying-linked-data-with-sparql
		String acceptHeader = request.getHeader("Accept");
		String format = "application/sparql-results+xml"; // default: application/sparql-results+xml --> change variable name to out!!!
		String file = "false"; // default: false
		String result = "";
		String query = "SELECT * WHERE { ?s ?p ?o } LIMIT 10";
		String repo = "";
		String type = "rdf4j";
		String serverURL = "";
		// PARSE PARAMETER
		if (request.getParameter("query") == null) {
			format = "html";
			result = "";
		} else {
			query = request.getParameter("query");
			// http://www.w3schools.com/jsref/jsref_encodeuricomponent.asp JavaScript encodeURIComponent() Function
			query = URLDecoder.decode(query, "UTF-8");
		}
		if (request.getParameter("format") != null) {
			format = request.getParameter("format");
		}
		if (request.getParameter("file") != null) {
			file = request.getParameter("file");
		}
		if (request.getParameter("type") != null) {
			type = request.getParameter("type");
		}
		if (request.getParameter("serverURL") != null) {
			serverURL = request.getParameter("serverURL");
		}
		if (request.getParameter("repo") != null) {
			repo = request.getParameter("repo");
		}
		if (acceptHeader.equals("application/sparql-results+xml")) {
			format = "application/sparql-results+xml";
		} else if (acceptHeader.equals("application/sparql-results+json")) {
			format = "application/sparql-results+json";
		} else {
			format = format;
		}
		if (format.equals("xml")) {
			response.setContentType("application/xml;charset=UTF-8");
			if (file.equals("true")) {
				response.setHeader("Content-disposition", "attachment;filename=sparql_result.xml");
			}
		} else if (format.equals("json")) {
			response.setContentType("application/json;charset=UTF-8");
			if (file.equals("true")) {
				response.setHeader("Content-disposition", "attachment;filename=sparql_result.json");
			}
		} else if (format.equals("csv")) {
			response.setContentType("text/plain;charset=UTF-8");
			if (file.equals("true")) {
				response.setHeader("Content-disposition", "attachment;filename=sparql_result.csv");
			}
		} else if (format.equals("application/sparql-results+xml")) {
			response.setContentType("application/sparql-results+xml;charset=UTF-8");
			if (file.equals("true")) {
				response.setHeader("Content-disposition", "attachment;filename=sparql_result.xml");
			}
		} else if (format.equals("application/sparql-results+json")) {
			response.setContentType("application/sparql-results+json;charset=UTF-8");
			if (file.equals("true")) {
				response.setHeader("Content-disposition", "attachment;filename=sparql_result.json");
			}
		} else if (format.equals("html")) {
			response.setContentType("text/html;charset=UTF-8");
		} else {
			response.setContentType("application/sparql-results+xml;charset=UTF-8");
		}
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setCharacterEncoding("UTF-8");
		ServletOutputStream outstream = response.getOutputStream();
		try {
			if (format.equals("html")) {
				outstream.write(result.getBytes(Charset.forName("UTF-8")));
			} else {
				if (format.equals("application/sparql-results+xml")) {
					format = "xml";
				} else if (format.equals("application/sparql-results+json")) {
					format = "json";
				}
				SPARQLqueryOutputFile(serverURL, repo, query, format, outstream);
			}
			response.setStatus(200);
		} catch (Exception e) {
			response.setContentType("application/json;charset=UTF-8");
			response.setStatus(500);
		} finally {
			outstream.flush();
			outstream.close();
		}
	}
	
	public static ServletOutputStream SPARQLqueryOutputFile(String serverURL, String repositoryID, String queryString, String format, ServletOutputStream out) {
		try {
			Repository repo = new HTTPRepository(serverURL, repositoryID);
			repo.initialize();
			RepositoryConnection con = repo.getConnection();
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			if ("xml".equals(format) || "XML".equals(format) || "Xml".equals(format)) {
				SPARQLResultsXMLWriter sparqlWriterXML = new SPARQLResultsXMLWriter(out);
				tupleQuery.evaluate(sparqlWriterXML);
			} else if ("json".equals(format) || "JSON".equals(format) || "Json".equals(format)) {
				SPARQLResultsJSONWriter sparqlWriterJSON = new SPARQLResultsJSONWriter(out);
				tupleQuery.evaluate(sparqlWriterJSON);
			} else if ("csv".equals(format) || "CSV".equals(format) || "Csv".equals(format)) {
				SPARQLResultsCSVWriter sparqlWriterCSV = new SPARQLResultsCSVWriter(out);
				tupleQuery.evaluate(sparqlWriterCSV);
			} else if ("tsv".equals(format) || "TSV".equals(format) || "Tsv".equals(format)) {
				SPARQLResultsTSVWriter sparqlWriterTSV = new SPARQLResultsTSVWriter(out);
				tupleQuery.evaluate(sparqlWriterTSV);
			} else {
				SPARQLResultsJSONWriter sparqlWriter = new SPARQLResultsJSONWriter(out);
				tupleQuery.evaluate(sparqlWriter);
			}
			con.close();
		} catch (Exception e) {
			throw new NullPointerException(e.getMessage());
		}
		return out;
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
			processRequest(request, response);
		
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

			processRequest(request, response);
		
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Servlet returns SPARQL XML/JSON/CSV/TSV from triplestore (repository labelingsystem";
	}// </editor-fold>

}
