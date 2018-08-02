package wife.application;

/**
 * Something technical went wrong from which we cannot recover.
 * This exception is used for the transactional proxy to issue a rollback
 */
public class ApplicationRuntimeException extends RuntimeException {
  public ApplicationRuntimeException(String message){
    super(message);
  }
}
