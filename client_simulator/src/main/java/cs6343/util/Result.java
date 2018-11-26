package cs6343.util;

/*
 * Holds whether operation was successful or not.
 * If not successful return message should contain an error message. 
 */
public class Result<T> {

	
	@Override
	public String toString() {
		return "Result [operationReturnMessage=" + operationReturnMessage + ", operationReturnVal=" + operationReturnVal
				+ ", operationSuccess=" + operationSuccess + "]";
	}

	public boolean isOperationSuccess() {
		return operationSuccess;
	}

	public void setOperationSuccess(boolean operationSuccess) {
		this.operationSuccess = operationSuccess;
	}

	public String getOperationReturnMessage() {
		return operationReturnMessage;
	}

	public void setOperationReturnMessage(String operationReturnMessage) {
		this.operationReturnMessage = operationReturnMessage;
	}

	public T getOperationReturnVal() {
		return operationReturnVal;
	}

	public void setOperationReturnVal(T operationReturnVal) {
		this.operationReturnVal = operationReturnVal;
	}

	String operationReturnMessage;
	T operationReturnVal;
	boolean operationSuccess;
}
