package com.jinyan.service;

import com.jinyan.model.ReissueInvoice;

public class ReissueInvoiceService {

	public boolean add(ReissueInvoice reissueInvoice) {
		return reissueInvoice.save();
	}
}
