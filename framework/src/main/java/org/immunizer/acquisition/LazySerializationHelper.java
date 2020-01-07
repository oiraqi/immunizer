package org.immunizer.acquisition;

import org.immunizer.acquisition.Invocation;

public class LazySerializationHelper {

    private static java.util.HashMap<String, Boolean> memory = new java.util.HashMap<String, Boolean>();

    /**
     * Checks whether we can go lazy on this. 
     * Current rule is to skip objects with java.sql.* interfaces.
     * Other rules are specific to OFBiz (quick'n dirty, should be moved elsewhere)
     * Other rules may be added as needed
     * 
     * @param object
     * @param fqn
     * @return
     */
    public static synchronized boolean skipInvocation(Invocation invocation) {
        String fqn = invocation.getFullyQualifiedMethodName();
        if (memory.containsKey(fqn))
            return memory.get(fqn).booleanValue();

        Object[] params = invocation.getParams();
        Object[] objects = null;
        if (params != null && invocation.returns()) {
            objects = new Object[params.length + 1];
            for (int i = 0; i < params.length; i++)
                objects[i] = params[i];
            objects[params.length] = invocation.getResult();
        } else if (params != null) {
            objects = new Object[params.length];
            for (int i = 0; i < params.length; i++)
                objects[i] = params[i];
        } else if (invocation.returns()) {
            objects = new Object[1];
            objects[0] = invocation.getResult();
        } else { // no params and no returned value, so skip it!
            memory.put(fqn, true);
            return true;
        }
        for (Object object : objects) {
            if(object == null)
                continue;
                      
            Class<?>[] interfaces = object.getClass().getInterfaces();
            for (Class<?> interfaz : interfaces)
                if (interfaz.getCanonicalName().startsWith("java.sql.")) {
                    memory.put(fqn, true);
                    return true;
                }
        }
        memory.put(fqn, false);
        return false;
    }

    /**
     * Deprecated The whole purpose of lazy serialization is to avoid calling such a
     * method. Indeed, instead of iterating over the sql result set and retrieve
     * data from the database, and return back the cursor to its original position
     * (-1), we will rather wait until the application itself retrieves the records.
     * At that point, we will intercept the data.
     * 
     * @param result
     * @return
     */
    /*
     * public static String toJsonTree(Object result) { StringWriter sw = new
     * StringWriter(); Gson gson = new Gson(); try { JsonWriter writer =
     * gson.newJsonWriter(sw); Method getMetaDataMethod =
     * result.getClass().getMethod("getMetaData"); Object meta =
     * getMetaDataMethod.invoke(result); Method getColumnCountMethod =
     * meta.getClass().getMethod("getColumnCount"); int cc = ((Integer)
     * getColumnCountMethod.invoke(meta)).intValue(); writer.beginArray(); Method
     * nextMethod = result.getClass().getMethod("next"); Method beforeFirstMethod =
     * result.getClass().getMethod("beforeFirst"); Method getObjectMethod =
     * result.getClass().getMethod("getObject", int.class); Method
     * getColumnNameMethod = meta.getClass().getMethod("getColumnName", int.class);
     * Method getColumnClassNameMethod =
     * meta.getClass().getMethod("getColumnClassName", int.class); while ((Boolean)
     * nextMethod.invoke(result)) { writer.beginObject(); for (int i = 1; i <= cc;
     * ++i) { writer.name((String) getColumnNameMethod.invoke(meta, i)); Class<?>
     * type = Class.forName((String) getColumnClassNameMethod.invoke(meta, i));
     * gson.toJson(getObjectMethod.invoke(result, i), type, writer); //
     * writer.value(rs.getString(i)); } writer.endObject(); } writer.endArray();
     * writer.flush(); // Move back the cursor to initial position (-1). // May
     * throw an exception depending on the driver. // The whole method is to be
     * avoided anyway! beforeFirstMethod.invoke(result); } catch (Exception e) {
     * e.printStackTrace(); } return sw.toString(); }
     */
}