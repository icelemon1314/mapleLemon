package handling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import tools.HexTool;

public class ExternalCodeTableGetter {

    final Properties props;

    public ExternalCodeTableGetter(Properties properties) {
        this.props = properties;
    }

    private final static <T extends Enum<? extends WritableIntValueHolder> & WritableIntValueHolder> T valueOf(final String name, T[] values) {
        for (T val : values) {
            if (val.name().equals(name)) {
                return val;
            }
        }
        return null;
    }

    private final <T extends Enum<? extends WritableIntValueHolder> & WritableIntValueHolder> byte getValue(final String name, T[] values, final byte def) {
        String prop = this.props.getProperty(name);
        if ((prop != null) && (prop.length() > 0)) {
            String trimmed = prop.trim();
            String[] args = trimmed.split(" ");
            int base = 0;
            String offset;
            if (args.length == 2) {
                base = ((WritableIntValueHolder) valueOf(args[0], values)).getValue();
                if (base == def) {
                    base = getValue(args[0], values, def);
                }
                offset = args[1];
            } else {
                offset = args[0];
            }
            if ((offset.length() > 2) && (offset.substring(0, 2).equals("0x"))) {
                return (byte) (Short.parseShort(offset.substring(2), 16) + base);
            }
            return (byte) (Short.parseShort(offset) + base);
        }

        return def;
    }

    public final static <T extends Enum<? extends WritableIntValueHolder> & WritableIntValueHolder> String getOpcodeTable(T[] enumeration) {
        StringBuilder enumVals = new StringBuilder();
        List<T> all = new ArrayList<>();
        all.addAll(Arrays.asList(enumeration));
        Collections.sort(all, new Comparator<WritableIntValueHolder>() {
            @Override
            public int compare(WritableIntValueHolder o1, WritableIntValueHolder o2) {
                return Byte.valueOf(o1.getValue()).compareTo(o2.getValue());
            }
        });
        for (Enum code : all) {
            enumVals.append(code.name());
            enumVals.append(" = ");
            enumVals.append("0x");
            enumVals.append(HexTool.toString(((WritableIntValueHolder) code).getValue()));
            enumVals.append(" (");
            enumVals.append(((WritableIntValueHolder) code).getValue());
            enumVals.append(")\n");
        }
        return enumVals.toString();
    }

    public final static <T extends Enum<? extends WritableIntValueHolder> & WritableIntValueHolder> void populateValues(Properties properties, T[] values) {
        ExternalCodeTableGetter exc = new ExternalCodeTableGetter(properties);
        for (Enum code : values) {
            ((WritableIntValueHolder) code).setValue(exc.getValue(code.name(), values, (byte) -1));
        }
    }
}
