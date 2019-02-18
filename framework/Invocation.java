import com.google.common.hash.Hashing;

public class Invocation {
	private String version;
	private String fullyQualifiedMethodName;
	private StackTraceElement[] callStack;
	private Object[] params;
	private Object result;
	private boolean _returns;
	private boolean _returnsString;
	private boolean exception;
	private long startTime;
	private long endTime;
	private long executionTime;
	private String threadTag;
	private int callStackId;

	public Invocation(String version, String fullyQualifiedMethodName, Object[] params) {
		startTime = System.currentTimeMillis();
		this.version = version;
		this.fullyQualifiedMethodName = fullyQualifiedMethodName;
		this.params = params;
		exception = false;
		_returns = fullyQualifiedMethodName.indexOf(" void ") == -1;
		_returnsString = fullyQualifiedMethodName.indexOf("java.lang.String ") > 0
				&& fullyQualifiedMethodName.indexOf("java.lang.String ") < fullyQualifiedMethodName.indexOf("(")
				&& (fullyQualifiedMethodName.indexOf("<") == -1
						|| fullyQualifiedMethodName.indexOf("<") > fullyQualifiedMethodName.indexOf("("))
				&& (fullyQualifiedMethodName.indexOf("[") == -1
						|| fullyQualifiedMethodName.indexOf("[") > fullyQualifiedMethodName.indexOf("("));
	}

	public void update(Object result, boolean exception) {
		this.result = result;
		this.exception = exception;
		endTime = System.currentTimeMillis();
		executionTime = endTime - startTime;
		Thread currentThread = Thread.currentThread();
		threadTag = currentThread.getName().substring(currentThread.getName().indexOf('#') + 1);
		callStack = currentThread.getStackTrace();
		StringBuffer sb = new StringBuffer();
		for (StackTraceElement stackElement : callStack) {
			sb.append(stackElement.toString());
			sb.append("\n");
		}
		callStackId = Math.abs(Hashing.adler32().hashBytes(sb.toString().getBytes()).asInt());
	}

	public int getCallStackId() {
		return callStackId;
	}

	public String getThreadTag() {
		return threadTag;
	}

	public String getFullyQualifiedMethodName() {
		return fullyQualifiedMethodName;
	}

	public String getVersion() {
		return version;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public boolean getException() {
		return exception;
	}

	public boolean returns() {
		return _returns;
	}

	public boolean returnsString() {
		return _returnsString;
	}

	public int getNumberOfParams() {
		return params == null ? 0 : params.length;
	}

	public Object[] getParams() {
		return params;
	}

	public Object getResult() {
		return result;
	}

	public StackTraceElement[] getCallStack() {
		return callStack;
	}
}