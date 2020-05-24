package com.debugers.alltv.exception;

import com.debugers.alltv.result.CodeMsg;

/**
 * 全局异常
 * @author hsl
 *
 */


public class GlobalException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private CodeMsg mg;
	
	//构造函数
	public GlobalException(CodeMsg mg) {
		super(mg.toString());
		this.mg=mg;
	}

	public CodeMsg getMg() {
		return mg;
	}
	
	
}
