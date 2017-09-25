package org.totschnig.myexpenses.util.licence;

import android.content.Context;
import android.support.annotation.NonNull;

import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.model.Money;
import org.totschnig.myexpenses.util.CurrencyFormatter;
import org.totschnig.myexpenses.util.LicenceHandler;

import java.util.Currency;

public enum Package {
  Contrib(300), Upgrade(200), Extended(500), Professional_6(500), Professional_36(2000);

  public long getDefaultPrice() {
    return defaultPrice;
  }

  private final long defaultPrice;

  Package(long price) {
    this.defaultPrice = price;
  }

  public boolean isProfessional() {
    return name().startsWith("Professional");
  }

  public String getFormattedPrice(Context context) {
    String formatted = CurrencyFormatter.instance().formatCurrency(
        new Money(Currency.getInstance("EUR"), getDefaultPrice()));
    if (isProfessional()) {
      formatted += " / " + getDuration(context);
    }

    return formatted;
  }

  @NonNull
  private String getDuration(Context context) {
    return isProfessional() ?
        context.getString(R.string.n_months, name().substring(name().lastIndexOf("_") + 1)) : "";
  }

  public String getButtonLabel(Context context) {
    switch (this) {
      case Contrib:
        return context.getString(LicenceHandler.LicenceStatus.CONTRIB.getResId());
      case Upgrade:
        return  context.getString(R.string.pref_contrib_purchase_title_upgrade);
      case Extended:
        return context.getString(LicenceHandler.LicenceStatus.EXTENDED.getResId());
      default:
       return String.format("%s (%s)",
           context.getString(LicenceHandler.LicenceStatus.PROFESSIONAL.getResId()),
           getDuration(context));
    }
  }
}