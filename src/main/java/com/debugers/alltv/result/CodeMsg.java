package com.debugers.alltv.result;

public class CodeMsg {

	private Integer code;
	private String msg;

	// 通用
	public static CodeMsg SUCCESS = new CodeMsg(200, "success");
	public static CodeMsg SERVER_ERROR = new CodeMsg(500100, "error");
	public static CodeMsg BIND_ERROR = new CodeMsg(400, "参数校验异常:%s");
	public static CodeMsg ROOM_ERROR = new CodeMsg(500101, "房间不存在");
	public static CodeMsg PAGE_ERROR = new CodeMsg(404, "分类不存在");

	private CodeMsg(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public Integer getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

	/*
	 * 遍参
	 */
	public CodeMsg fillArgs(Object... Object) {
		int code = this.code;
		String message = String.format(this.msg, Object);
		return new CodeMsg(code, message);
	}
}
