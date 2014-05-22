package net.maeph.lucene;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;


public final class PhoneNumberFilter extends TokenFilter {


        private CharTermAttribute charTermAttr;

        protected PhoneNumberFilter(TokenStream ts) {
            super(ts);
            this.charTermAttr = addAttribute(CharTermAttribute.class);
        }

        @Override
        public boolean incrementToken() throws IOException {
            if (!input.incrementToken()) {
                return false;
            }
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            try {
                String numberToParse = new String(charTermAttr.buffer());
                Phonenumber.PhoneNumber number = phoneUtil.parse(numberToParse, "PL");
                String normalizedNumber = ("+" + number.getCountryCode() + number.getNationalNumber());
                System.out.println(normalizedNumber);
                charTermAttr.setEmpty();
                charTermAttr.copyBuffer(normalizedNumber.toCharArray(), 0, normalizedNumber.length());
            } catch (NumberParseException e) {
            }
            return true;
        }
}
