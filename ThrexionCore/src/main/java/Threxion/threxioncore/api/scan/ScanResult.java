package Threxion.threxioncore.api.scan;

import Threxion.threxioncore.api.property.PropertyValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 扫描结果：ScanManager 收集各 ScanProvider 的产出，并允许 ScanEvent 追加内容。
 */
public class ScanResult {

    private final ScanContext context;
    private final List<ScanEntry> entries = new ArrayList<>();

    public ScanResult(ScanContext context) {
        this.context = context;
    }

    public ScanResult add(String name, PropertyValue value, String source) {
        entries.add(new ScanEntry(name, value, source));
        return this;
    }

    public ScanResult add(String name, double value, String source) {
        entries.add(new ScanEntry(name, PropertyValue.ofNumber(value), source));
        return this;
    }

    public ScanResult add(String name, String value, String source) {
        entries.add(new ScanEntry(name, PropertyValue.ofString(value), source));
        return this;
    }

    public ScanContext getContext() {
        return context;
    }

    public List<ScanEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    /** 格式化为可读文本（Debug / 命令展示）。 */
    public String format() {
        StringBuilder sb = new StringBuilder();
        for (ScanEntry e : entries) {
            sb.append(e.getName()).append(" = ").append(e.getValue())
                    .append("  [").append(e.getSource()).append("]\n");
        }
        return sb.toString();
    }
}
