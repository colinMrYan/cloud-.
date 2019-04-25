package com.inspur.emmcloud.util.common.richtext.markdown;

import android.text.SpannableStringBuilder;
import android.util.Pair;
import android.util.SparseArray;

import com.zzhoujay.markdown.parser.Line;
import com.zzhoujay.markdown.parser.LineQueue;
import com.zzhoujay.markdown.parser.StyleBuilder;
import com.zzhoujay.markdown.parser.Tag;
import com.zzhoujay.markdown.parser.TagHandler;
import com.zzhoujay.markdown.style.CodeSpan;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhou on 16-7-10.
 * TagHandlerImpl
 */
public class TagHandlerImpl implements TagHandler {

    private static final Pattern patternH1_2 = Pattern.compile("^\\s*=+\\s*$");
    private static final Pattern patternH2_2 = Pattern.compile("^\\s*-+\\s*$");

    private static final Pattern patternH = Pattern.compile("^\\s*#{1,6} \\s*([^#]*)(\\s*#)?");
    private static final Pattern patternH1 = Pattern.compile("^\\s*# \\s*([^#]*)(\\s*#)?");
    private static final Pattern patternH2 = Pattern.compile("^\\s*#{2} \\s*([^#]*)(\\s*#)?");
    private static final Pattern patternH3 = Pattern.compile("^\\s*#{3} \\s*([^#]*)(\\s*#)?");
    private static final Pattern patternH4 = Pattern.compile("^\\s*#{4} \\s*([^#]*)(\\s*#)?");
    private static final Pattern patternH5 = Pattern.compile("^\\s*#{5} \\s*([^#]*)(\\s*#)?");
    private static final Pattern patternH6 = Pattern.compile("^\\s*#{6} \\s*([^#]*)(\\s*#)?");


    private static final Pattern patternQuota = Pattern.compile("^\\s{0,3}>\\s(.*)");
    private static final Pattern patternUl = Pattern.compile("^\\s*[*+-]\\s+(.*)");
    private static final Pattern patternOl = Pattern.compile("^\\s*\\d+\\.\\s+(.*)");

    private static final Pattern patternItalic = Pattern.compile("[^*_]*(([*_])([^*_].*?)\\2)");
    private static final Pattern patternEm = Pattern.compile("[^*_]*(([*_])\\2([^*_].*?)\\2\\2)");
    private static final Pattern patternEmItalic = Pattern.compile("[^*_]*(([*_])\\2\\2([^*_].*?)\\2\\2\\2)");
    private static final Pattern patternDelete = Pattern.compile("[^~]*((~{2,4})([^~].*?)\\2)");
    private static final Pattern patternCode = Pattern.compile("[^`]*((`+)([^`].*?)\\2)");
    //jason修改处  修改Url匹配正则表达式
    private static final Pattern patternLink = Pattern.compile(".*?(\\[\\s*([^\\[]*)\\s*]\\(\\s*(\\S*?)(\\s+(['\"])([^\\)]*)\\5)?\\s*?\\))");
    private static final Pattern patternImage = Pattern.compile(".*?(!\\[\\s*(.*?)\\s*]\\(\\s*(\\S*?)(\\s+(['\"])(.*?)\\5)?\\s*?\\))");
    private static final Pattern patternLink2 = Pattern.compile(".*?(\\[\\s*(.*?)\\s*]\\s*\\[\\s*(.*?)\\s*])");
    private static final Pattern patternLinkId = Pattern.compile("^\\s*\\[\\s*(.*?)\\s*]:\\s*(\\S+?)(\\s+(['\"])(.*?)\\4)?\\s*$");
    private static final Pattern patternImage2 = Pattern.compile(".*?(!\\[\\s*(.*?)\\s*]\\s*\\[\\s*(.*?)\\s*])");
    private static final Pattern patternImageId = Pattern.compile("^\\s*!\\[\\s*(.*?)\\s*]:\\s*(\\S+?)(\\s+(['\"])(.*?)\\4)?\\s*$");

    private static final Pattern patternEmail = Pattern.compile(".*?(<(\\S+@\\S+\\.\\S+)>).*?");
    private static final Pattern patternAutoLink = Pattern.compile("((https|http|ftp|rtsp|mms)?://)[^\\s]+");

    private static final Pattern patternEndSpace = Pattern.compile("(.*?) {2} *$");
    private static final Pattern patternInlineSpace = Pattern.compile("\\S*(\\s+)\\S+");

    private static final Pattern patternCodeBlock = Pattern.compile("^( {4}|\\t)(.*)");
    private static final Pattern patternCodeBlock2 = Pattern.compile("^\\s*```");

    private static final Pattern patternBlankLine = Pattern.compile("^\\s*$");

    private static final Pattern patternGap = Pattern.compile("^\\s*([-*]\\s*){3,}$");

    private static final SparseArray<Pattern> patterns = new SparseArray<>();// matcher缓冲区

    static {
        patterns.put(Tag.CODE_BLOCK_1, patternCodeBlock);
        patterns.put(Tag.CODE_BLOCK_2, patternCodeBlock2);
        patterns.put(Tag.H1, patternH1);
        patterns.put(Tag.H2, patternH2);
        patterns.put(Tag.H3, patternH3);
        patterns.put(Tag.H4, patternH4);
        patterns.put(Tag.H5, patternH5);
        patterns.put(Tag.H6, patternH6);
        patterns.put(Tag.H, patternH);
        patterns.put(Tag.QUOTA, patternQuota);
        patterns.put(Tag.UL, patternUl);
        patterns.put(Tag.OL, patternOl);
        patterns.put(Tag.EM, patternEm);
        patterns.put(Tag.ITALIC, patternItalic);
        patterns.put(Tag.EM_ITALIC, patternEmItalic);
        patterns.put(Tag.EMAIL, patternEmail);
        patterns.put(Tag.AUTO_LINK, patternAutoLink);
        patterns.put(Tag.DELETE, patternDelete);
        patterns.put(Tag.LINK, patternLink);
        patterns.put(Tag.LINK2, patternLink2);
        patterns.put(Tag.LINK_ID, patternLinkId);
        patterns.put(Tag.IMAGE, patternImage);
        patterns.put(Tag.IMAGE2, patternImage2);
        patterns.put(Tag.IMAGE_ID, patternImageId);
        patterns.put(Tag.BLANK, patternBlankLine);
        patterns.put(Tag.NEW_LINE, patternEndSpace);
        patterns.put(Tag.GAP, patternGap);
        patterns.put(Tag.H1_2, patternH1_2);
        patterns.put(Tag.H2_2, patternH2_2);
        patterns.put(Tag.CODE, patternCode);
    }

    private StyleBuilder styleBuilder;
    private QueueProvider queueProvider;
    private HashMap<String, Pair<String, String>> idLinkLinks;
    private HashMap<String, Pair<String, String>> idImageUrl;


    public TagHandlerImpl(StyleBuilder styleBuilder) {
        this.styleBuilder = styleBuilder;
        idImageUrl = new HashMap<>();
        idLinkLinks = new HashMap<>();
    }

    @Override
    public boolean h(Line line) {
        return h6(line) || h5(line) || h4(line) || h3(line) || h2(line) || h1(line);
    }

    @Override
    public boolean h1(Line line) {
        Matcher matcher = obtain(Tag.H1, line.getSource());
        if (matcher != null && matcher.find()) {
            line.setType(Line.LINE_TYPE_H1);
            line.setStyle(SpannableStringBuilder.valueOf(matcher.group(1)));
            inline(line);
            line.setStyle(styleBuilder.h1(line.getStyle()));
            return true;
        }
        return false;
    }

    @Override
    public boolean h2(Line line) {
        Matcher matcher = obtain(Tag.H2, line.getSource());
        if (matcher.find()) {
            line.setType(Line.LINE_TYPE_H2);
            line.setStyle(SpannableStringBuilder.valueOf(matcher.group(1)));
            inline(line);
            line.setStyle(styleBuilder.h2(line.getStyle()));
            return true;
        }
        return false;
    }

    @Override
    public boolean h3(Line line) {
        Matcher matcher = obtain(Tag.H3, line.getSource());
        if (matcher.find()) {
            line.setType(Line.LINE_TYPE_H3);
            line.setStyle(SpannableStringBuilder.valueOf(matcher.group(1)));
            inline(line);
            line.setStyle(styleBuilder.h3(line.getStyle()));

            return true;
        }
        return false;
    }

    @Override
    public boolean h4(Line line) {
        Matcher matcher = obtain(Tag.H4, line.getSource());
        if (matcher.find()) {
            line.setType(Line.LINE_TYPE_H4);
            line.setStyle(SpannableStringBuilder.valueOf(matcher.group(1)));
            inline(line);
            line.setStyle(styleBuilder.h4(line.getStyle()));

            return true;
        }
        return false;
    }

    @Override
    public boolean h5(Line line) {
        Matcher matcher = obtain(Tag.H5, line.getSource());
        if (matcher.find()) {
            line.setType(Line.LINE_TYPE_H5);
            line.setStyle(SpannableStringBuilder.valueOf(matcher.group(1)));
            inline(line);
            line.setStyle(styleBuilder.h5(line.getStyle()));

            return true;
        }
        return false;
    }

    @Override
    public boolean h6(Line line) {
        Matcher matcher = obtain(Tag.H6, line.getSource());
        if (matcher.find()) {
            line.setType(Line.LINE_TYPE_H6);
            line.setStyle(SpannableStringBuilder.valueOf(matcher.group(1)));
            inline(line);
            line.setStyle(styleBuilder.h6(line.getStyle()));

            return true;
        }
        return false;
    }

    @Override
    public boolean quota(Line line) {
        LineQueue queue = queueProvider.getQueue();
        line = line.get();

        Matcher matcher = obtain(Tag.QUOTA, line.getSource());
        if (matcher.find()) {
            line.setType(Line.LINE_TYPE_QUOTA);
            Line child = line.createChild(matcher.group(1));
            line.attachChildToNext();
            line.attachChildToPrev();

            Line prev = queue.prevLine();
            if (line.parentLine() == null && prev != null && prev.getType() == Line.LINE_TYPE_QUOTA) {
                SpannableStringBuilder style = new SpannableStringBuilder(" ");
                styleBuilder.quota(style);
                while (prev.childLine() != null && prev.childLine().getType() == Line.LINE_TYPE_QUOTA) {
                    prev = prev.childLine();
                    styleBuilder.quota(style);
                }
                prev.copyToNext();
                queue.prevLine().setStyle(style);
            }
            if (quota(child) || ul(child) || ol(child) || h(child)) {
                if (child.getHandle() == Line.HANDLE_BY_ROOT) {
                    if (line.parentLine() == null) {
                        if (child.getData() == Line.LINE_TYPE_UL) {
                            line.setStyle(styleBuilder.ul2(child.getStyle(), findCount(Tag.QUOTA, line, 1) - 1, child.getAttr()));
                        } else {
                            line.setStyle(styleBuilder.ol2(child.getStyle(), findCount(Tag.QUOTA, line, 1) - 1, child.getAttr(), child.getCount()));
                        }
                    } else {
                        line.setData(child.getData());
                        line.setStyle(child.getStyle());
                        line.setAttr(child.getAttr());
                        line.setCount(child.getCount());
                        line.setHandle(Line.HANDLE_BY_ROOT);
                    }
                    return true;
                }
            } else {
                child.setStyle(SpannableStringBuilder.valueOf(child.getSource()));
                inline(child);
            }

            line.setStyle(styleBuilder.quota(child.getStyle()));
            return true;
        }
        return false;
    }

    @Override
    public boolean ul(Line line) {
        return ul(line, 0);
    }

    private boolean ul(Line line, int level) {
        Matcher matcher = obtain(Tag.UL, line.getSource());
        if (matcher.find()) {
            line.setType(Line.LINE_TYPE_UL);
            Line line1 = line.createChild(matcher.group(1));
            line.setAttr(0);

            Line parent = line.parentLine();
            LineQueue queue = queueProvider.getQueue();
            Line prev = line.prevLine();

            boolean inQuota = queue.currLine().getType() == Line.LINE_TYPE_QUOTA;
            if (inQuota) {
                line.setHandle(Line.HANDLE_BY_ROOT);
                line.setData(Line.LINE_TYPE_UL);
            }


            if (prev != null && (prev.getType() == Line.LINE_TYPE_OL || prev.getType() == Line.LINE_TYPE_UL)) {
                if (level > 0) {
                    line.setAttr(level);
                } else {
                    String m = line.getSource().substring(matcher.start(), matcher.start(1) - 2);
                    m = m.replaceAll("\\t", "    ");
                    if (m.length() > prev.getAttr() * 2 + 1)
                        line.setAttr(prev.getAttr() + 1);
                    else
                        line.setAttr(m.length() / 2);
                }

            }
            if (inQuota) {
                line.setStyle(" ");
            } else {
                line.setStyle(styleBuilder.ul(" ", line.getAttr()));
            }
            if (find(Tag.UL, line1)) {
                int nextLevel = line.getAttr() + 1;
                line1.unAttachFromParent();

                if (parent != null) {
                    Line p = parent.copyToNext();
                    p.addChild(line1);
                    queue.next();
                    ul(line1, nextLevel);
                    if (inQuota) {
                        while (p.parentLine() != null) {
                            p = p.parentLine();
                        }
                        p.setStyle(styleBuilder.ul2(line1.getStyle(), findCount(Tag.QUOTA, p, 1) - 1, line1.getAttr()));
                    } else {
                        while (p != null && p.getType() == Line.LINE_TYPE_QUOTA) {
                            p.setStyle(styleBuilder.quota(line1.getStyle()));
                            p = p.parentLine();
                        }
                    }
                } else {
                    line.addNext(line1);
                    queue.next();
                    ul(queue.currLine(), nextLevel);
                }

                return true;
            }
            if (find(Tag.OL, line1)) {
                int nextLevel = line.getAttr() + 1;
                line1.unAttachFromParent();

                if (parent != null) {
                    Line p = parent.copyToNext();
                    p.addChild(line1);
                    queue.next();
                    ol(line1, nextLevel);
                    if (inQuota) {
                        while (p.parentLine() != null) {
                            p = p.parentLine();
                        }
                        p.setStyle(styleBuilder.ol2(line1.getStyle(), findCount(Tag.QUOTA, p, 1) - 1, line1.getAttr(), line1.getCount()));
                    } else {
                        while (p != null && p.getType() == Line.LINE_TYPE_QUOTA) {
                            p.setStyle(styleBuilder.quota(line1.getStyle()));
                            p = p.parentLine();
                        }
                    }

                } else {
                    line.addNext(line1);
                    queue.next();
                    ol(queue.currLine(), nextLevel);
                }

                return true;
            }

            CharSequence userText;
            if (h(line1)) {
                userText = line1.getStyle();
            } else {
                userText = line1.getSource();
            }
            SpannableStringBuilder builder;
            if (userText instanceof SpannableStringBuilder) {
                builder = (SpannableStringBuilder) userText;
            } else {
                builder = new SpannableStringBuilder(userText);
            }
            line.setStyle(builder);
            inline(line);
            if (!inQuota) {
                line.setStyle(styleBuilder.ul(line.getStyle(), line.getAttr()));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean ol(Line line) {
        return ol(line, 0);
    }

    private boolean ol(Line line, int level) {
        Matcher matcher = obtain(Tag.OL, line.getSource());
        if (matcher.find()) {
            line.setType(Line.LINE_TYPE_OL);
            Line line1 = new Line(matcher.group(1));
            line.setAttr(0);

            Line parent = line.parentLine();
            LineQueue queue = queueProvider.getQueue();
            Line prev = line.prevLine();

            boolean inQuota = queue.currLine().getType() == Line.LINE_TYPE_QUOTA;
            if (inQuota) {
                line.setHandle(Line.HANDLE_BY_ROOT);
                line.setData(Line.LINE_TYPE_OL);
            }

            if (prev != null && (prev.getType() == Line.LINE_TYPE_OL || prev.getType() == Line.LINE_TYPE_UL)) {
                if (level > 0) {
                    line.setAttr(level);
                } else {
                    String m = line.getSource().substring(matcher.start(), matcher.start(1) - 2);
                    m = m.replaceAll("\\t", "    ");
                    if (m.length() > prev.getAttr() * 2 + 1)
                        line.setAttr(prev.getAttr() + 1);
                    else
                        line.setAttr(m.length() / 2);
                }

            }
            if (prev != null && prev.getType() == Line.LINE_TYPE_OL && prev.getAttr() == line.getAttr()) {
                line.setCount(prev.getCount() + 1);
            } else {
                line.setCount(1);
            }
            if (inQuota) {
                line.setStyle(" ");
            } else {
                line.setStyle(styleBuilder.ol(" ", line.getAttr(), line.getCount()));
            }
            if (find(Tag.UL, line1)) {
                int nextLevel = line.getAttr() + 1;
                line1.unAttachFromParent();

                if (parent != null) {
                    Line p = parent.copyToNext();
                    p.addChild(line1);
                    queue.next();
                    ul(line1, nextLevel);
                    if (inQuota) {
                        while (p.parentLine() != null) {
                            p = p.parentLine();
                        }
                        p.setStyle(styleBuilder.ul2(line1.getStyle(), findCount(Tag.QUOTA, p, 1) - 1, line1.getAttr()));
                    } else {
                        while (p != null && p.getType() == Line.LINE_TYPE_QUOTA) {
                            p.setStyle(styleBuilder.quota(line1.getStyle()));
                            p = p.parentLine();
                        }
                    }
                } else {
                    line.addNext(line1);
                    queue.next();
                    ul(queue.currLine(), nextLevel);
                }

                return true;
            }
            if (find(Tag.OL, line1)) {
                int nextLevel = line.getAttr() + 1;
                line1.unAttachFromParent();

                if (parent != null) {
                    Line p = parent.copyToNext();
                    p.addChild(line1);
                    queue.next();
                    ol(line1, nextLevel);
                    if (inQuota) {
                        while (p.parentLine() != null) {
                            p = p.parentLine();
                        }
                        p.setStyle(styleBuilder.ol2(line1.getStyle(), findCount(Tag.QUOTA, p, 1) - 1, line1.getAttr(), line1.getCount()));
                    } else {
                        while (p != null && p.getType() == Line.LINE_TYPE_QUOTA) {
                            p.setStyle(styleBuilder.quota(line1.getStyle()));
                            p = p.parentLine();
                        }
                    }
                } else {
                    line.addNext(line1);
                    queue.next();
                    ol(queue.currLine(), nextLevel);
                }

                return true;
            }

            CharSequence userText;
            if (h(line1)) {
                userText = line1.getStyle();
            } else {
                userText = line1.getSource();
            }
            SpannableStringBuilder builder;
            if (userText instanceof SpannableStringBuilder) {
                builder = (SpannableStringBuilder) userText;
            } else {
                builder = new SpannableStringBuilder(userText);
            }
            line.setStyle(builder);
            inline(line);

            if (!inQuota) {
                line.setStyle(styleBuilder.ol(line.getStyle(), line.getAttr(), line.getCount()));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean gap(Line line) {
        line = line.get();
        Matcher matcher = obtain(Tag.GAP, line.getSource());
        if (matcher.matches()) {
            line.setType(Line.LINE_TYPE_GAP);
            line.setStyle(styleBuilder.gap());
            return true;
        }
        return false;
    }

    @Override
    public boolean em(Line line) {
        line = line.get();
        SpannableStringBuilder builder = (SpannableStringBuilder) line.getStyle();
        Matcher matcher = obtain(Tag.EM, builder);
        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);
            if (checkInCode(builder, start, end)) {
                continue;
            }
            SpannableStringBuilder sb = (SpannableStringBuilder) builder.subSequence(matcher.start(3), matcher.end(3));
            builder.delete(matcher.start(1), matcher.end(1));
            builder.insert(matcher.start(1), styleBuilder.em(sb));
            em(line);
            return true;
        }
        return false;
    }

    @Override
    public boolean italic(Line line) {
        line = line.get();
        SpannableStringBuilder builder = (SpannableStringBuilder) line.getStyle();
        Matcher matcher = obtain(Tag.ITALIC, builder);
        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);
            if (checkInCode(builder, start, end)) {
                continue;
            }
            SpannableStringBuilder sb = (SpannableStringBuilder) builder.subSequence(matcher.start(3), matcher.end(3));
            builder.delete(matcher.start(1), matcher.end(1));
            builder.insert(matcher.start(1), styleBuilder.italic(sb));
            italic(line);
            return true;
        }
        return false;
    }

    @Override
    public boolean emItalic(Line line) {
        line = line.get();
        SpannableStringBuilder builder = (SpannableStringBuilder) line.getStyle();
        Matcher matcher = obtain(Tag.EM_ITALIC, builder);
        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);
            if (checkInCode(builder, start, end)) {
                continue;
            }
            SpannableStringBuilder sb = (SpannableStringBuilder) builder.subSequence(matcher.start(3), matcher.end(3));
            builder.delete(matcher.start(1), matcher.end(1));
            builder.insert(matcher.start(1), styleBuilder.emItalic(sb));
            emItalic(line);
            return true;
        }
        return false;
    }

    @Override
    public boolean code(Line line) {
//        line = line.get();
//        SpannableStringBuilder builder = (SpannableStringBuilder) line.getStyle();
//        Matcher matcher = obtain(Tag.CODE, builder);
//        if (matcher.find()) {
//            String content = matcher.group(3);
//            builder.delete(matcher.startWebSocket(1), matcher.end(1));
//            builder.insert(matcher.startWebSocket(1), styleBuilder.code(content));
//            code(line);
//            return true;
//        }
        return false;
    }

    @Override
    public boolean email(Line line) {
//        line = line.get();
//        SpannableStringBuilder builder = (SpannableStringBuilder) line.getStyle();
//        Matcher matcher = obtain(Tag.EMAIL, builder);
//        if (matcher.find()) {
//            SpannableStringBuilder sb = (SpannableStringBuilder) builder.subSequence(matcher.startWebSocket(2), matcher.end(2));
//            builder.delete(matcher.startWebSocket(1), matcher.end(1));
//            builder.insert(matcher.startWebSocket(1), styleBuilder.email(sb));
//            email(line);
//            return true;
//        }
        return false;
    }

    @Override
    public boolean delete(Line line) {
        line = line.get();
        SpannableStringBuilder builder = (SpannableStringBuilder) line.getStyle();
        Matcher matcher = obtain(Tag.DELETE, builder);
        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);
            if (checkInCode(builder, start, end)) {
                continue;
            }
            SpannableStringBuilder sb = (SpannableStringBuilder) builder.subSequence(matcher.start(3), matcher.end(3));
            builder.delete(matcher.start(1), matcher.end(1));
            builder.insert(matcher.start(1), styleBuilder.delete(sb));
            delete(line);
            return true;
        }
        return false;
    }

    @Override
    public boolean autoLink(Line line) {
//        line = line.get();
//        SpannableStringBuilder builder = (SpannableStringBuilder) line.getStyle();
//        Matcher matcher = obtain(Tag.AUTO_LINK, builder);
        boolean m = false;
//        while (matcher.find()) {
//            String content = matcher.group();
//            builder.delete(matcher.startWebSocket(), matcher.end());
//            builder.insert(matcher.startWebSocket(), styleBuilder.link(content, content, ""));
//            m = true;
//        }
        return m;
    }

    @Override
    public boolean link(Line line) {
        line = line.get();
        SpannableStringBuilder builder = (SpannableStringBuilder) line.getStyle();
        Matcher matcher = obtain(Tag.LINK, builder);
        if (matcher.find()) {
            String title = matcher.group(2);
            String link = matcher.group(3);
            String hint = matcher.group(6);
            builder.delete(matcher.start(1), matcher.end(1));
            builder.insert(matcher.start(1), styleBuilder.link(title, link, hint));
            link(line);
            return true;
        }
        return false;
    }

    @Override
    public boolean link2(Line line) {
        line = line.get();
        SpannableStringBuilder builder = (SpannableStringBuilder) line.getStyle();
        Matcher matcher = obtain(Tag.LINK2, builder);
        if (matcher.find()) {
            String title = matcher.group(2);
            String id = matcher.group(3);
            Pair<String, String> link = idLinkLinks.get(id);
            if (link != null) {
                builder.delete(matcher.start(1), matcher.end(1));
                builder.insert(matcher.start(1), styleBuilder.link(title, link.first, link.second));
            } else {
                return false;
            }
            link2(line);
            return true;
        }
        return false;
    }

    @Override
    public boolean linkId(String line) {
        Matcher matcher = obtain(Tag.LINK_ID, line);
        if (matcher.find()) {
            String id = matcher.group(1);
            String link = matcher.group(2);
            String hint = matcher.group(5);
            idLinkLinks.put(id, new Pair<>(link, hint));
            return true;
        }
        return false;
    }

    //jason修改处
    @Override
    public boolean image(Line line) {
        line = line.get();
        SpannableStringBuilder builder = (SpannableStringBuilder) line.getStyle();
        Matcher matcher = obtain(Tag.IMAGE, builder);
        if (matcher.find()) {
            String title = matcher.group(2);
            String link = matcher.group(3);
            String hint = matcher.group(6);
            builder.delete(matcher.start(1), matcher.end(1));
            builder.insert(matcher.start(1), styleBuilder.image(title, link, hint));
            image(line);
            return true;
        }
        return false;
    }

    //jason修改处
    @Override
    public boolean image2(Line line) {
        line = line.get();
        SpannableStringBuilder builder = (SpannableStringBuilder) line.getStyle();
        Matcher matcher = obtain(Tag.IMAGE2, builder);
        if (matcher.find()) {
            String title = matcher.group(2);
            String id = matcher.group(3);
            Pair<String, String> image = idImageUrl.get(id);
            if (image != null) {
                builder.delete(matcher.start(1), matcher.end(1));
                builder.insert(matcher.start(1), styleBuilder.image(title, image.first, image.second));
            } else {
                return false;
            }
            image2(line);
            return true;
        }
        return false;
    }

    //jason修改处
    @Override
    public boolean imageId(String line) {
        Matcher matcher = obtain(Tag.IMAGE_ID, line);
        if (matcher.find()) {
            String id = matcher.group(1);
            String link = matcher.group(2);
            String hint = matcher.group(5);
            idImageUrl.put(id, new Pair<>(link, hint));
            return true;
        }
        return false;
    }

    //jason修改处
    @Override
    public boolean codeBlock1(Line line) {
//        Matcher matcher = obtain(Tag.CODE_BLOCK_1, line.getSource());
//        if (matcher.find()) {
//            String content = matcher.group(2);
//            LineQueue queue = queueProvider.getQueue();
//            Line next = queue.nextLine();
//            StringBuilder sb = new StringBuilder(content);
//            StringBuilder bsb = new StringBuilder();
//
//            while (next != null) {
//                CharSequence r = get(Tag.CODE_BLOCK_1, next, 2);
//                if (r == null) {
//                    if (find(Tag.BLANK, next)) {
//                        bsb.append(' ').append('\n');
//                    } else {
//                        break;
//                    }
//                } else {
//                    if (bsb.length() != 0) {
//                        sb.append(bsb).append(r);
//                        bsb.delete(0, sb.length());
//                    } else {
//                        sb.append('\n').append(r);
//                    }
//                }
//                queue.removeNextLine();
//                next = queue.nextLine();
//            }
//
//
//            line.setType(Line.LINE_TYPE_CODE_BLOCK_1);
//            line.setStyle(styleBuilder.codeBlock(sb.toString()));
//            return true;
//        }
        return false;
    }


    //jason修改处
    @Override
    public boolean codeBlock2(Line line) {
//        if (find(Tag.CODE_BLOCK_2, line)) {
//            LineQueue queue = queueProvider.getQueue();
//            LineQueue nextQueue = queue.copy();
//            boolean find = false;
//            while (nextQueue.nextLine() != null) {
//                if (find(Tag.CODE_BLOCK_2, nextQueue.nextLine())) {
//                    nextQueue.next();
//                    removePrevBlankLine(nextQueue);
//                    removeNextBlankLine(queue);
//                    find = true;
//                    break;
//                }
//                nextQueue.next();
//            }
//            if (find) {
//                StringBuilder sb = new StringBuilder();
//                queue.next();
//                queue.removePrevLine();
//                while (queue.currLine() != nextQueue.currLine()) {
//                    sb.append(queue.currLine().getSource()).append('\n');
//                    queue.next();
//                    queue.removePrevLine();
//                }
//                removeNextBlankLine(nextQueue);
//                nextQueue.currLine().setType(Line.LINE_TYPE_CODE_BLOCK_2);
//                nextQueue.currLine().setStyle(styleBuilder.codeBlock(sb.toString()));
//                return true;
//            } else {
//                return false;
//            }
//        }
        return false;
    }

    //jason修改处
    @Override
    public boolean inline(Line line) {
        //jason  修改 去除一些标签的过滤
        boolean flag = false;
//        flag = email(line) || flag;
        flag = image(line) || flag;
        flag = image2(line) || flag;
        flag = link(line) || flag;
//        flag = link2(line) || flag;
//        flag = autoLink(line) || flag;
        flag = emItalic(line) || flag;
        flag = em(line) || flag;
        flag = italic(line) || flag;
//        flag = delete(line) || flag;
//        boolean flag = link(line) || delete(line)  || em(line) ||italic(line);
        return flag;
    }

    @Override
    public boolean find(int tag, Line line) {
        return line != null && find(tag, line.getSource());
    }

    @Override
    public boolean find(int tag, String line) {
        if (line == null) {
            return false;
        }
        Matcher m = obtain(tag, line);
        return m != null && m.find();
    }

    @Override
    public int findCount(int tag, Line line, int group) {
        return line == null ? 0 : findCount(tag, line.getSource(), group);
    }

    @Override
    public int findCount(int tag, String line, int group) {
        if (line == null) {
            return 0;
        }

        Matcher matcher = obtain(tag, line);
        if (matcher == null) {
            return 0;
        }
        if (matcher.find()) {
            return findCount(tag, matcher.group(group), group) + 1;
        }

        return 0;
    }

    private boolean checkInCode(SpannableStringBuilder builder, int start, int end) {
        CodeSpan[] css = builder.getSpans(0, builder.length(), CodeSpan.class);
        for (CodeSpan cs : css) {
            int c_start = builder.getSpanStart(cs);
            int c_end = builder.getSpanEnd(cs);
            if (!(c_start >= end || c_end <= start)) {
                return true;
            }
        }
        return false;
    }

    private Matcher obtain(int tag, CharSequence src) {
        Matcher m = patterns.get(tag, null).matcher(src);
        return m;
    }

    public void setQueueProvider(QueueProvider queueProvider) {
        this.queueProvider = queueProvider;
    }


    @Override
    public CharSequence get(int tag, Line line, int group) {
        return get(tag, line.getSource(), group);
    }

    @Override
    public CharSequence get(int tag, CharSequence line, int group) {
        Matcher m = obtain(tag, line);
        return m.find() ? m.group(group) : null;
    }

    private void removeNextBlankLine(LineQueue queue) {
        while (queue.nextLine() != null) {
            if (find(Tag.BLANK, queue.nextLine())) {
                queue.removeNextLine();
            } else {
                return;
            }
        }
    }

    private void removePrevBlankLine(LineQueue queue) {
        while (queue.prevLine() != null) {
            if (find(Tag.BLANK, queue.prevLine())) {
                queue.removePrevLine();
            } else {
                return;
            }
        }
    }


}
