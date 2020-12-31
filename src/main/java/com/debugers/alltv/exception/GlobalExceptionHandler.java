package com.debugers.alltv.exception;

import com.debugers.alltv.result.CodeMsg;
import com.debugers.alltv.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.UnexpectedTypeException;
import java.util.List;

/**
 * 全局异常处理程序
 * 
 */
@Slf4j
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

	/**
	 * 自定义错误
	 * 
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(value = GlobalException.class)
	public Result<String> parameterException(GlobalException exception) {
		CodeMsg mg = exception.getMg();
		log.error("错误信息:" + mg.getMsg());
		return Result.error(mg);
	}

	/**
	 * validation错误
	 * 
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(value = BindException.class)
	public Result<String> parameterException(BindException exception) {
		List<ObjectError> allErrors = exception.getAllErrors();
		// 获取第一个
		ObjectError error = allErrors.get(0);
		// 错误信息
		String mg = error.getDefaultMessage();
		log.error("错误信息:" + mg);
		return Result.error(CodeMsg.BIND_ERROR.fillArgs(mg));
	}

	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public Result<String> parameterException(MethodArgumentNotValidException exception) {
		BindingResult bindingResult = exception.getBindingResult();
		List<ObjectError> allErrors = bindingResult.getAllErrors();
		ObjectError error = allErrors.get(0);
		// 错误信息
		String mg = error.getDefaultMessage();
		log.error("错误信息:" + mg);
		return Result.error(CodeMsg.BIND_ERROR.fillArgs(mg));
	}

	@ExceptionHandler(value = UnexpectedTypeException.class)
	public Result<String> parameterException(UnexpectedTypeException exception) {
		// 错误信息
		String mg = "参数错误";
		log.error("错误信息:" + mg);
		return Result.error(CodeMsg.BIND_ERROR.fillArgs(mg));
	}


	@ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
	public Result<String> parameterException(HttpRequestMethodNotSupportedException exception) {
		String mg = "请求方式错误";
		log.error("错误信息:" + mg);
		return Result.error(CodeMsg.BIND_ERROR.fillArgs(mg));
	}

	@ExceptionHandler(value = HttpMessageNotReadableException.class)
	public Result<String> parameterException(HttpMessageNotReadableException exception) {
		String mg = "Required request body is missing";
		log.error("错误信息:" + mg);
		return Result.error(CodeMsg.BIND_ERROR.fillArgs(mg));
	}

	@ExceptionHandler(value = MissingServletRequestParameterException.class)
	public Result<String> parameterException(MissingServletRequestParameterException exception) {
		String mg = "Required request body is missing";
		log.error("错误信息:" + mg);
		return Result.error(CodeMsg.BIND_ERROR.fillArgs(mg));
	}

//	@ExceptionHandler(value = Exception.class)
//	public Result<String> exception(Exception exception) {
//		log.error("错误信息", exception.fillInStackTrace());
//		return Result.error(CodeMsg.SERVER_ERROR);
//	}

}
