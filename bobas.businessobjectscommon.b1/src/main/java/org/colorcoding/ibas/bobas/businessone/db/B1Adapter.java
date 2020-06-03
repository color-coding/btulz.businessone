package org.colorcoding.ibas.bobas.businessone.db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.colorcoding.ibas.bobas.businessone.MyConfiguration;
import org.colorcoding.ibas.bobas.common.IConditions;
import org.colorcoding.ibas.bobas.common.ICriteria;
import org.colorcoding.ibas.bobas.common.ISorts;
import org.colorcoding.ibas.bobas.common.ISqlQuery;
import org.colorcoding.ibas.bobas.common.SqlQuery;
import org.colorcoding.ibas.bobas.db.DbAdapterFactory;
import org.colorcoding.ibas.bobas.db.IDbAdapter;
import org.colorcoding.ibas.bobas.db.ISqlScripts;
import org.colorcoding.ibas.bobas.db.ParsingException;
import org.colorcoding.ibas.bobas.db.SqlScripts;
import org.colorcoding.ibas.bobas.i18n.I18N;
import org.colorcoding.ibas.bobas.serialization.SerializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sap.smb.sbo.api.ICompany;
import com.sap.smb.sbo.api.SBOCOMConstants;

public class B1Adapter implements IB1Adapter {

	public static IB1Adapter create(ICompany company) {
		Integer dbHana = -1;
		try {
			dbHana = (Integer) SBOCOMConstants.class.getField("BoDataServerTypes_dst_HANADB")
					.get(SBOCOMConstants.class);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			return new B1Adapter(company, new BOAdapter());
		}
		if (dbHana != -1 && company.getDbServerType() == dbHana) {
			IDbAdapter dbAdapter = DbAdapterFactory.create().createAdapter("hana");
			return new B1Adapter(company, (BOAdapter) dbAdapter.createBOAdapter());
		}
		return new B1Adapter(company, new BOAdapter());
	}

	public B1Adapter(ICompany b1Company) {
		this.setB1Company(b1Company);
	}

	public B1Adapter(ICompany b1Company, BOAdapter adapter) {
		this(b1Company);
		this.setAdapter(adapter);
	}

	private BOAdapter adapter;

	protected BOAdapter getAdapter() {
		return adapter;
	}

	private void setAdapter(BOAdapter adapter) {
		this.adapter = adapter;
	}

	private ICompany b1Company;

	protected ICompany getB1Company() {
		if (this.b1Company == null) {
			throw new SerializationException(I18N.prop("msg_b1_invalid_company"));
		}
		return b1Company;
	}

	private void setB1Company(ICompany b1Company) {
		this.b1Company = b1Company;
	}

	@Override
	public ISqlQuery parseSqlQuery(ICriteria criteria) throws ParsingException {
		return this.getAdapter().parseSqlQuery(criteria);
	}

	@Override
	public ISqlQuery parseSqlQuery(IConditions conditions) throws ParsingException {
		return this.getAdapter().parseSqlQuery(conditions);
	}

	@Override
	public ISqlQuery parseSqlQuery(ISorts sorts) throws ParsingException {
		return this.getAdapter().parseSqlQuery(sorts);
	}

	@Override
	public ISqlQuery parseSqlQuery(ICriteria criteria, Integer boCode) throws ParsingException {
		try {
			// 获取主表
			String table = this.getMasterTable(boCode);
			if (table == null || table.isEmpty()) {
				throw new ParsingException(I18N.prop("msg_bobas_not_found_bo_table", boCode));
			}
			return this.parseSqlQuery(criteria, table);
		} catch (ParsingException e) {
			throw e;
		} catch (Exception e) {
			throw new ParsingException(e);
		}
	}

	@Override
	public ISqlQuery parseSqlQuery(ICriteria criteria, String table) throws ParsingException {
		// 拼接语句
		String order = this.getAdapter().parseSqlQuery(criteria.getSorts()).getQueryString();
		String where = this.getAdapter().parseSqlQuery(criteria.getConditions()).getQueryString();
		ISqlScripts sqlScripts = this.getAdapter().getSqlScripts();
		return new SqlQuery(sqlScripts.groupSelectQuery("*", table, where, order, criteria.getResultCount()));
	}

	private Map<Integer, String> tableMap = new HashMap<>();

	protected String getMasterTable(Integer boCode) throws SAXException, IOException, ParserConfigurationException {
		String table = this.tableMap.get(boCode);
		if (table != null) {
			return table;
		}
		table = this.getMasterTable(newDocument(this.getB1Company().getBusinessObjectXmlSchema(boCode)));
		if (table != null) {
			this.tableMap.put(boCode, table);
		}
		return table;
	}

	protected Document newDocument(String xmlData) throws SAXException, IOException, ParserConfigurationException {
		try (InputStream stream = new ByteArrayInputStream(xmlData.getBytes("utf-8"))) {
			return this.newDocument(stream);
		}
	}

	protected Document newDocument(InputStream stream) throws SAXException, IOException, ParserConfigurationException {
		InputSource source = new InputSource(stream);
		source.setEncoding(MyConfiguration.getConfigValue(MyConfiguration.CONFIG_ITEM_B1_DATA_ENCODING, "utf-8"));
		return this.newDocument(source);
	}

	protected Document newDocument(InputSource source) throws SAXException, IOException, ParserConfigurationException {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);
	}

	protected String getMasterTable(Document document) throws SAXException, IOException, ParserConfigurationException {
		Node node = document.getFirstChild();// schema
		node = node.getFirstChild();// element name="BOM"
		node = node.getFirstChild();// complexType
		node = node.getFirstChild();// sequence
		node = node.getFirstChild();// element name="BO"
		node = node.getFirstChild();// complexType
		node = node.getFirstChild();// all
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			node = nodes.item(i);
			if (node.getNodeName().equals("element")) {
				Node attrib = node.getAttributes().getNamedItem("name");
				if (attrib == null) {
					continue;
				}
				if (attrib.getNodeValue() == null) {
					continue;
				}
				if (attrib.getNodeValue().equals("AdmInfo")) {
					continue;
				}
				if (attrib.getNodeValue().equals("QueryParams")) {
					continue;
				}
				// 第一个表，主表
				return attrib.getNodeValue();
			}
		}
		return null;
	}
}

class BOAdapter extends org.colorcoding.ibas.bobas.db.BOAdapter {

	private ISqlScripts sqlScripts = null;

	@Override
	public ISqlScripts getSqlScripts() {
		if (this.sqlScripts == null) {
			this.sqlScripts = new SqlScripts();
		}
		return this.sqlScripts;
	}

}