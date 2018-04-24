package org.colorcoding.ibas.bobas.businessone;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.colorcoding.ibas.bobas.businessone.data.DataWrapping;
import org.colorcoding.ibas.bobas.businessone.serialization.B1SerializerFactory;
import org.colorcoding.ibas.bobas.businessone.serialization.IB1Serializer;
import org.colorcoding.ibas.bobas.common.Criteria;
import org.colorcoding.ibas.bobas.common.ICriteria;
import org.colorcoding.ibas.bobas.common.IOperationResult;
import org.colorcoding.ibas.bobas.core.RepositoryException;

import com.sap.smb.sbo.api.ICompany;
import com.sap.smb.sbo.api.IItems;
import com.sap.smb.sbo.api.IRecordset;
import com.sap.smb.sbo.api.SBOCOMConstants;
import com.sap.smb.sbo.api.SBOCOMException;
import com.sap.smb.sbo.api.SBOCOMUtil;

import junit.framework.TestCase;

public class testBORepository extends TestCase {

	public String getToken() {
		return "";
	}

	private void saveFile(String fileName, String data) throws IOException {
		ByteArrayInputStream stream = new ByteArrayInputStream(data.getBytes());
		OutputStream os = new FileOutputStream(fileName);
		int bytesRead = 0;
		byte[] buffer = new byte[8192];
		while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
			os.write(buffer, 0, bytesRead);
		}
		os.close();
		stream.close();
	}

	public void testB1Data() throws SBOCOMException, IOException, RepositoryException {
		BORepositoryDemo boRepository = new BORepositoryDemo();
		boRepository.openRepository();
		ICompany b1Company = boRepository.getCompany();
		String fileGroup = MyConfiguration.getWorkFolder() + File.separator + "recordset_";
		this.saveFile(fileGroup + "schema.xml",
				b1Company.getBusinessObjectXmlSchema(SBOCOMConstants.BoObjectTypes_BoRecordset));
		System.out.println("schema: " + fileGroup + "schema.xml");
		IRecordset recordset = SBOCOMUtil.newRecordset(b1Company);
		recordset.doQuery("Select * from OITM");
		recordset.saveXML(fileGroup + "data.xml");
		System.out.println("data: " + fileGroup + "data.xml");

		fileGroup = MyConfiguration.getWorkFolder() + File.separator + "items_";
		this.saveFile(fileGroup + "schema.xml",
				b1Company.getBusinessObjectXmlSchema(SBOCOMConstants.BoObjectTypes_oItems));
		System.out.println("schema: " + fileGroup + "schema.xml");
		IItems item = SBOCOMUtil.getItems(b1Company, String.valueOf(recordset.getFields().item("ItemCode").getValue()));
		item.saveXML(fileGroup + "data.xml");
		System.out.println("data: " + fileGroup + "data.xml");
		// 测试克隆
		IB1Serializer<?> serializer = B1SerializerFactory.create().createManager().create(b1Company, "xml");
		// item = serializer.clone(item);
		// System.out.println(item.getAsXML());
		ByteArrayOutputStream writer = new ByteArrayOutputStream();
		serializer.getSchema(IItems.class, writer);
		System.out.println(writer.toString());
		boRepository.closeRepository();
	}

	public void testFetch() {
		ICriteria criteria = new Criteria();
		criteria.setResultCount(1);
		BORepositoryDemo boRepository = new BORepositoryDemo();
		IOperationResult<DataWrapping> operationResult = boRepository.fetchItem(criteria, this.getToken());
		assertEquals(operationResult.getMessage(), 0, operationResult.getResultCode());
		for (DataWrapping data : operationResult.getResultObjects()) {
			System.out.println(data.getContent());
		}
	}

}