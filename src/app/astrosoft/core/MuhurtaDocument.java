package app.astrosoft.core;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.MuhurtaProfile;
import app.astrosoft.beans.NotesOwner;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;
import app.astrosoft.consts.XmlConsts;
import app.astrosoft.export.XMLHelper;
import app.astrosoft.util.AstroUtil;

public class MuhurtaDocument implements NotesOwner {

    private String name;
    private Date date;
    private Date time;
    private Place place;
    private MuhurtaProfile person1;
    private MuhurtaProfile person2;
    private String notes;
    private String filePath;
    private boolean modified;

    public MuhurtaDocument() {
        this.name = "Muhurta";
        this.date = new Date();
        this.time = this.date;
        this.place = app.astrosoft.ui.AstroSoft.getPreferences().getPlace();
    }

    public MuhurtaDocument(String name, Date date, Date time, Place place, MuhurtaProfile p1, MuhurtaProfile p2) {
        this.name = (name != null && !name.trim().isEmpty()) ? name.trim() : "Muhurta";
        this.date = (date != null) ? date : new Date();
        this.time = (time != null) ? time : this.date;
        this.place = place;
        this.person1 = p1;
        this.person2 = p2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date != null ? new Date(date.getTime()) : null;
    }

    public void setDate(Date date) {
        this.date = (date != null) ? new Date(date.getTime()) : null;
    }

    public Date getTime() {
        return time != null ? new Date(time.getTime()) : null;
    }

    public void setTime(Date time) {
        this.time = (time != null) ? new Date(time.getTime()) : null;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public MuhurtaProfile getPerson1() {
        return person1;
    }

    public void setPerson1(MuhurtaProfile person1) {
        this.person1 = person1;
    }

    public MuhurtaProfile getPerson2() {
        return person2;
    }

    public void setPerson2(MuhurtaProfile person2) {
        this.person2 = person2;
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String getNotesOwnerTitle() {
        String baseName = (name != null && !name.trim().isEmpty()) ? name : "Muhurta";
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
            return baseName + " [" + df.format(date) + "]";
        }
        return baseName;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public void setFilePath(String filePath) {
        this.filePath = (filePath != null) ? new File(filePath).getAbsolutePath() : null;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public void saveToFile(String fileName) {
        Document doc = XMLHelper.createDOM();
        Element root = XMLHelper.addRootElement(doc, "Muhurta");
        root.setAttribute("version", "2.0");

        Element eventEl = doc.createElement("Event");
        XMLHelper.addElement(doc, eventEl, "Name", name != null ? name : "Muhurta");

        Calendar cal = Calendar.getInstance();
        if (place != null) {
            cal.setTimeZone(place.astrosoftTimeZone().getTimeZone());
        }
        if (date != null) {
            cal.setTime(date);
        }
        if (time != null) {
            Calendar tCal = Calendar.getInstance();
            if (place != null) {
                tCal.setTimeZone(place.astrosoftTimeZone().getTimeZone());
            }
            tCal.setTime(time);
            cal.set(Calendar.HOUR_OF_DAY, tCal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, tCal.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, tCal.get(Calendar.SECOND));
        }

        XMLHelper.addElement(doc, eventEl, "Year", String.valueOf(cal.get(Calendar.YEAR)));
        XMLHelper.addElement(doc, eventEl, "Month", String.valueOf(cal.get(Calendar.MONTH) + 1));
        XMLHelper.addElement(doc, eventEl, "Date", String.valueOf(cal.get(Calendar.DATE)));
        XMLHelper.addElement(doc, eventEl, "Hour", String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
        XMLHelper.addElement(doc, eventEl, "Minute", String.valueOf(cal.get(Calendar.MINUTE)));
        XMLHelper.addElement(doc, eventEl, "Second", String.valueOf(cal.get(Calendar.SECOND)));
        XMLHelper.addElement(doc, eventEl, "DateTime", AstroUtil.formatDateTime(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND)));

        if (place != null) {
            eventEl.appendChild(place.toXMLElement(doc));
        }
        root.appendChild(eventEl);

        if (person1 != null) {
            root.appendChild(serializePerson(doc, "Person1", person1));
        }
        if (person2 != null) {
            root.appendChild(serializePerson(doc, "Person2", person2));
        }

        if (notes != null && !notes.trim().isEmpty()) {
            Element notesEl = doc.createElement(XmlConsts.Notes);
            notesEl.appendChild(doc.createCDATASection(notes));
            root.appendChild(notesEl);
        }

        XMLHelper.saveXML(doc, fileName);
        this.filePath = new File(fileName).getAbsolutePath();
        this.modified = false;
    }

    private Element serializePerson(Document doc, String tagName, MuhurtaProfile profile) {
        Element el = doc.createElement(tagName);
        if (profile.hasHoroscope()) {
            el.setAttribute("type", "Horoscope");
            BirthData bd = profile.getHoroscope().getBirthData();
            // Clone birthData without notes so private notes are not duplicated
            BirthData cleanBd = new BirthData(bd.name(), bd.sex(), bd.birthDay(), bd.getBirthPlace());
            el.appendChild(cleanBd.toXMLElement(doc, XmlConsts.BirthData));
        } else {
            el.setAttribute("type", "QuickEntry");
            XMLHelper.addElement(doc, el, XmlConsts.Name, profile.getName());
            if (profile.getRasi() != null) {
                XMLHelper.addElement(doc, el, XmlConsts.Rasi, profile.getRasi().name());
            }
            if (profile.getNakshathra() != null) {
                XMLHelper.addElement(doc, el, XmlConsts.Nakshathra, profile.getNakshathra().name());
            }
        }
        return el;
    }

    public static MuhurtaDocument createFromFile(String fileName) {
        try {
            Document doc = XMLHelper.parseXML(fileName);
            if (doc == null || doc.getDocumentElement() == null) {
                return null;
            }
            Element root = doc.getDocumentElement();
            if (!"Muhurta".equalsIgnoreCase(root.getNodeName())) {
                return null;
            }

            MuhurtaDocument mDoc = new MuhurtaDocument();
            mDoc.setFilePath(new File(fileName).getAbsolutePath());

            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                String nodeName = node.getNodeName();

                if ("Event".equalsIgnoreCase(nodeName)) {
                    parseEvent(node, mDoc);
                } else if ("Person1".equalsIgnoreCase(nodeName)) {
                    mDoc.setPerson1(parsePerson(node));
                } else if ("Person2".equalsIgnoreCase(nodeName)) {
                    mDoc.setPerson2(parsePerson(node));
                } else if (XmlConsts.Notes.equalsIgnoreCase(nodeName)) {
                    mDoc.setNotes(node.getTextContent());
                }
            }
            mDoc.setModified(false);
            return mDoc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static MuhurtaProfile parsePerson(Node personNode) {
        if (personNode == null) return null;
        Element el = (Element) personNode;
        String type = el.getAttribute("type");
        if ("Horoscope".equalsIgnoreCase(type)) {
            NodeList list = el.getElementsByTagName(XmlConsts.BirthData);
            if (list.getLength() > 0) {
                BirthData bd = BirthData.valueOfXMLNode(list.item(0));
                if (bd != null) {
                    return new MuhurtaProfile(new Horoscope(bd));
                }
            }
        } else {
            String name = "";
            Rasi rasi = null;
            Nakshathra nak = null;
            NodeList ch = el.getChildNodes();
            for (int j = 0; j < ch.getLength(); j++) {
                Node n = ch.item(j);
                if (XmlConsts.Name.equalsIgnoreCase(n.getNodeName())) {
                    name = n.getTextContent();
                } else if (XmlConsts.Rasi.equalsIgnoreCase(n.getNodeName())) {
                    try { rasi = Rasi.valueOf(n.getTextContent().trim()); } catch (Exception ignored) {}
                } else if (XmlConsts.Nakshathra.equalsIgnoreCase(n.getNodeName())) {
                    try { nak = Nakshathra.valueOf(n.getTextContent().trim()); } catch (Exception ignored) {}
                }
            }
            if (rasi != null && nak != null) {
                return new MuhurtaProfile(name, rasi, nak);
            }
        }
        return null;
    }

    private static void parseEvent(Node eventNode, MuhurtaDocument doc) {
        NodeList ch = eventNode.getChildNodes();
        int year = 2026, month = 1, day = 1, hour = 12, min = 0, sec = 0;
        boolean hasYMD = false;
        String dateTimeStr = null;
        Place pl = null;
        String evName = null;

        for (int i = 0; i < ch.getLength(); i++) {
            Node n = ch.item(i);
            String name = n.getNodeName();
            if ("Name".equalsIgnoreCase(name)) {
                evName = n.getTextContent();
            } else if ("Year".equalsIgnoreCase(name)) {
                try { year = Integer.parseInt(n.getTextContent().trim()); hasYMD = true; } catch (Exception ignored) {}
            } else if ("Month".equalsIgnoreCase(name)) {
                try { month = Integer.parseInt(n.getTextContent().trim()); hasYMD = true; } catch (Exception ignored) {}
            } else if ("Date".equalsIgnoreCase(name)) {
                try { day = Integer.parseInt(n.getTextContent().trim()); hasYMD = true; } catch (Exception ignored) {}
            } else if ("Hour".equalsIgnoreCase(name)) {
                try { hour = Integer.parseInt(n.getTextContent().trim()); } catch (Exception ignored) {}
            } else if ("Minute".equalsIgnoreCase(name)) {
                try { min = Integer.parseInt(n.getTextContent().trim()); } catch (Exception ignored) {}
            } else if ("Second".equalsIgnoreCase(name)) {
                try { sec = Integer.parseInt(n.getTextContent().trim()); } catch (Exception ignored) {}
            } else if ("DateTime".equalsIgnoreCase(name)) {
                dateTimeStr = n.getTextContent();
            } else if (XmlConsts.Place.equalsIgnoreCase(name)) {
                pl = Place.valueOfXMLNode(n);
            }
        }

        if (evName != null && !evName.trim().isEmpty()) {
            doc.setName(evName.trim());
        }
        if (pl != null) {
            doc.setPlace(pl);
        }

        Calendar cal = Calendar.getInstance();
        if (pl != null) {
            cal.setTimeZone(pl.astrosoftTimeZone().getTimeZone());
        }
        if (hasYMD) {
            cal.set(year, month - 1, day, hour, min, sec);
            cal.set(Calendar.MILLISECOND, 0);
            doc.setDate(cal.getTime());
            doc.setTime(cal.getTime());
        } else if (dateTimeStr != null) {
            Date d = AstroUtil.parseDateTime(dateTimeStr);
            if (d != null) {
                doc.setDate(d);
                doc.setTime(d);
            }
        }
    }
}
