package testing.support;

import java.util.HashMap;
import java.util.Map;

public class StatefullActionword {

  protected final KafkaEventConsumer eventConsumer;
  private final Map<String, Object> state;

  public StatefullActionword(KafkaEventConsumer eventConsumer){
    this.eventConsumer = eventConsumer;
    state  = new HashMap<>();
  }


  protected <T> T getState(String key, Class<T> type){
    return type.cast(state.get(key));
  }

  protected void addState(String key, Object value){
    state.put(key, value);
  }

  protected void clearState(){
    state.clear();
  }

}
