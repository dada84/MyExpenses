package org.totschnig.myexpenses.test.espresso;

import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.ExpenseEdit;
import org.totschnig.myexpenses.model.Account;
import org.totschnig.myexpenses.model.AccountType;
import org.totschnig.myexpenses.provider.DatabaseConstants;
import org.totschnig.myexpenses.testutils.Matchers;

import java.util.Currency;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.totschnig.myexpenses.activity.ExpenseEdit.KEY_NEW_TEMPLATE;
import static org.totschnig.myexpenses.contract.TransactionsContract.Transactions.OPERATION_TYPE;
import static org.totschnig.myexpenses.contract.TransactionsContract.Transactions.TYPE_SPLIT;
import static org.totschnig.myexpenses.contract.TransactionsContract.Transactions.TYPE_TRANSACTION;
import static org.totschnig.myexpenses.contract.TransactionsContract.Transactions.TYPE_TRANSFER;
import static org.totschnig.myexpenses.testutils.Espresso.checkEffectiveGone;
import static org.totschnig.myexpenses.testutils.Espresso.checkEffectiveVisible;

public class ExpenseEditTest {

  @Rule
  public ActivityTestRule<ExpenseEdit> mActivityRule =
      new ActivityTestRule<>(ExpenseEdit.class, false, false);
  private static String accountLabel1 = "Test label 1";
  private static String accountLabel2 = "Test label 2";
  private static Account account1;
  private static Account account2;
  private static Currency currency1 = Currency.getInstance("USD");
  private static Currency currency2 = Currency.getInstance("EUR");

  @Before
  public void fixture() {
    account1 = new Account(accountLabel1, currency1, 0, "", AccountType.CASH, Account.DEFAULT_COLOR);
    account1.save();
    account2 = new Account(accountLabel2, currency2, 0, "", AccountType.CASH, Account.DEFAULT_COLOR);
    account2.save();
  }

  @After
  public void tearDown() throws RemoteException, OperationApplicationException {
    Account.delete(account1.getId());
    Account.delete(account2.getId());
  }

  @Test
  public void formForTransactionIsPrepared() {
    Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), ExpenseEdit.class);
    i.putExtra(OPERATION_TYPE, TYPE_TRANSACTION);
    mActivityRule.launchActivity(i);
    checkEffectiveVisible(R.id.DateTimeRow, R.id.AmountRow, R.id.CommentRow, R.id.CategoryRow,
        R.id.PayeeRow, R.id.AccountRow, R.id.Recurrence);
  }

  @Test
  public void formForTransferIsPrepared() {
    Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), ExpenseEdit.class);
    i.putExtra(OPERATION_TYPE, TYPE_TRANSFER);
    mActivityRule.launchActivity(i);
    checkEffectiveVisible(R.id.DateTimeRow, R.id.AmountRow, R.id.CommentRow, R.id.AccountRow,
        R.id.TransferAccountRow, R.id.Recurrence);
  }

  @Test
  public void formForSplitIsPrepared() {
    Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), ExpenseEdit.class);
    i.putExtra(OPERATION_TYPE, TYPE_SPLIT);
    mActivityRule.launchActivity(i);
    checkEffectiveVisible(R.id.DateTimeRow, R.id.AmountRow, R.id.CommentRow, R.id.SplitContainer,
        R.id.PayeeRow, R.id.AccountRow, R.id.Recurrence);
  }

  @Test
  public void formForTemplateIsPrepared() {
    Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), ExpenseEdit.class);
    i.putExtra(OPERATION_TYPE, TYPE_TRANSACTION);
    i.putExtra(KEY_NEW_TEMPLATE, true);
    mActivityRule.launchActivity(i);
    checkEffectiveVisible(R.id.TitleRow, R.id.AmountRow, R.id.CommentRow, R.id.CategoryRow,
        R.id.PayeeRow, R.id.AccountRow, R.id.Recurrence);
    checkEffectiveGone(R.id.Plan);
  }


  @Test
  public void accountIdInExtraShouldPopulateSpinner() {
    Account[] allAccounts = {account1, account2};
    for (Account a : allAccounts) {
      Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), ExpenseEdit.class);
      i.putExtra(OPERATION_TYPE, TYPE_TRANSACTION);
      i.putExtra(DatabaseConstants.KEY_ACCOUNTID, a.getId());
      mActivityRule.launchActivity(i);
      onView(withId(R.id.Account)).check(matches(Matchers.withSpinnerText(a.getLabel())));
      mActivityRule.getActivity().finish();
    }
  }

  @Test
  public void currencyInExtraShouldPopulateSpinner() {
    Currency[] allCurrencies = {currency1, currency2};
    for (Currency c : allCurrencies) {
      //we assume that Fixture has set up the default account with id 1
      Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), ExpenseEdit.class);
      i.putExtra(OPERATION_TYPE, TYPE_TRANSACTION);
      i.putExtra(DatabaseConstants.KEY_CURRENCY, c.getCurrencyCode());
      mActivityRule.launchActivity(i);
      assertEquals("Account is not selected", c, mActivityRule.getActivity().getCurrentAccount().currency);
      mActivityRule.getActivity().finish();
    }
  }


  @Test
  public void saveAsNewWorksMultipleTimesInARow() {
    Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), ExpenseEdit.class);
    i.putExtra(OPERATION_TYPE, TYPE_TRANSACTION);
    i.putExtra(DatabaseConstants.KEY_ACCOUNTID, account1.getId());
    mActivityRule.launchActivity(i);
    String success = mActivityRule.getActivity().getString(R.string.save_transaction_and_new_success);
    int times = 5;
    int amount = 2;
    for (int j = 0; j < times; j++) {
      onView(withId(R.id.AmountEditText)).perform(typeText(String.valueOf(amount)));
      onView(withId(R.id.SAVE_AND_NEW_COMMAND)).perform(click());
      onView(withText(success)).check(matches(isDisplayed()));
    }
    //we assume two fraction digits
    assertEquals("Transaction sum does not match saved transactions", account1.getTransactionSum(null), -amount * times * 100);
  }
}
