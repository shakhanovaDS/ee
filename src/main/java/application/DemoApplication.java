package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@SpringBootApplication
public class DemoApplication {

	private String getRates(String code, String date){
		URL url;
		if (date != null) {
			String[] currDate = date.split("-");
			date = "?date_req="+currDate[2]+"/"+currDate[1]+"/"+currDate[0];
		}
		try {
			url = new URL("http://www.cbr.ru/scripts/XML_daily.asp"+((date != null)? date : ""));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(connection.getInputStream());
			doc.getDocumentElement().normalize();

			NodeList list = doc.getElementsByTagName("Valute");
			for (int temp = 0; temp < list.getLength(); temp++) {
				Node node = list.item(temp);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					if (code.equals(element.getElementsByTagName("CharCode").item(0).getTextContent())) {
						System.out.println(doc.getDocumentElement().getAttribute("Date"));
						String[] currDate = doc.getDocumentElement().getAttribute("Date").split("\\.");
						System.out.println(currDate[0]);
						System.out.println(currDate[1]);
						System.out.println(currDate[2]);
						date = currDate[2]+"-"+currDate[1]+"-"+currDate[0];
						return "{\n" +
								"\t\"code\" : \"" + element.getElementsByTagName("CharCode").item(0).getTextContent() + "\"\n" +
								"\t\"rate\" : \"" + element.getElementsByTagName("Value").item(0).getTextContent().replace(",", ".") + "\"\n" +
								"\t\"date\" : \"" + date + "\"\n" +
								"}";
					}
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@RequestMapping(value = "/api/rate/{code}/{date}", method = GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getFoosBySimplePathWithPathVariables
			(@PathVariable String code, @PathVariable String date) {
		return getRates(code, date);
	}

	@RequestMapping(value = "/api/rate/{code}", method = GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getFoosBySimplePathWithPathVariables
			(@PathVariable String code) {
		return getRates(code, null);
	}
}
