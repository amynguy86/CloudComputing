package cs6343.util;

/*
 * Holds whether operation was successful or not.
 * If not successful return message should contain an error message else whatever the operation is suppose to return 
 */
public class Result<T> {
	public T getOperationReturnMessage() {
		return operationReturnMessage;
	}

	public void setOperationReturnMessage(T operationReturnMessage) {
		this.operationReturnMessage = operationReturnMessage;
	}

	public boolean isOperationSuccess() {
		return operationSuccess;
	}

	public void setOperationSuccess(boolean operationSuccess) {
		this.operationSuccess = operationSuccess;
	}

	T operationReturnMessage;
	boolean operationSuccess;
}
