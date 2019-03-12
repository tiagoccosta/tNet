package tNet.events;
import java.util.*;
import tNet.*;

public interface OnUpdateClientListListener
{
	public void run (List<ConnectionID> list);
}
