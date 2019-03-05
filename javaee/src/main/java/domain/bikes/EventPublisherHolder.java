package domain.bikes;

/**
 * This class has only one purpose: testing.
 * Since CDI lookup is from unmanaged beans is only possible via static calls
 * to <code>Cdi.current().select(...)</code>, this class will be overriden by
 * a test-class with the same name wich can use mocks
 */
public class EventPublisherHolder {

}
