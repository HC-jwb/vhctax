package hc.fms.api.addon.report.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import hc.fms.api.addon.vhctax.entity.VehicleTaxTask;

@Service
public class TaxFileExportService {
	private static String[] taxListColumnNames = { "Label", "Model", "Plate No.", "Type", "Payment No.", "Cost",
			"Due Date", "Status", "Notification", "Photo", "Payment Receipt", "Edit/Remove" };

	public ByteArrayInputStream exportToExcel(List<VehicleTaxTask> list) {
		
		Workbook xlsxWb = new XSSFWorkbook();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		Sheet sheet1 = xlsxWb.createSheet("FirstSheet");
		
		sheet1.setColumnWidth(0, 10000);
		sheet1.setColumnWidth(9, 10000);
		
		Row row = null;
		Cell cell = null;
		
		row = sheet1.createRow(0);
		
		cell = row.createCell(0);
		cell.setCellValue("1-1");
		
		cell = row.createCell(1);
		cell.setCellValue("1-2");
		
		row = sheet1.createRow(1);
		cell = row.createCell(0);
		cell.setCellValue("2-1");
		
		cell = row.createCell(1);
		cell.setCellValue("2-2");
		
		row = sheet1.createRow(2);
		cell = row.createCell(0);
		cell.setCellValue("3-1");
		
		cell = row.createCell(1);
		cell.setCellValue("3-2");
		
		try {
			xlsxWb.write(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ByteArrayInputStream(out.toByteArray());

	}
}