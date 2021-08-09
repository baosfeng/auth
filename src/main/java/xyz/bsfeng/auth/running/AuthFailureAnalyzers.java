package xyz.bsfeng.auth.running;

import xyz.bsfeng.auth.exception.AuthParamException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

public class AuthFailureAnalyzers extends AbstractFailureAnalyzer<AuthParamException> {

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, AuthParamException cause) {
		return new FailureAnalysis("参数校验", cause.getField() + "不能为空", cause);
	}
}
