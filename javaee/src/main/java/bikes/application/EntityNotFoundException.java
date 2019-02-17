package bikes.application;

public class EntityNotFoundException extends Exception {
  protected EntityNotFoundException(Class type, long id){
    super(String.format("No entity of type %s found with id %s!", type, id));
  }
}
