

import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score


df = pd.read_csv('bodyfat.csv')
df.head()

# @title Density

from matplotlib import pyplot as plt
df['Density'].plot(kind='hist', bins=20, title='Density')
plt.gca().spines[['top', 'right',]].set_visible(False)

import seaborn as sns

# outlier
sns.boxplot(data=cd[['BodyFat','Weight', 'Abdomen','Neck','Chest','Wrist']])

df['PREDICTED_BODYFAT'] = 495 / df['Density'] - 450

# calcul diferenta absoluta intre grasimea corporala prezisa si cea masurata
df['DIFF_BODYFAT'] = abs(df['PREDICTED_BODYFAT'] - df['BodyFat'])

df_sorted = df.sort_values(by='DIFF_BODYFAT', ascending=False)
outliers = df_sorted.head(15)

print("Outliers:")
print(outliers[['Density', 'BodyFat', 'PREDICTED_BODYFAT', 'DIFF_BODYFAT']])

outliers_high_bodyfat = df[df['BodyFat'] > 45]
outliers_high_bodyfat

outlier_indices = [47,75,181,215]
df_cleaned = df.drop(index=outlier_indices)
df_cleaned.head()

df['PREDICTED_BMI'] = 0.45359237 * df['Weight'] / (df['Height'] * 0.0254) ** 2
outliers_high_predicted_bmi = df[df['PREDICTED_BMI'] > 40]
outliers_high_predicted_bmi[['BodyFat', 'PREDICTED_BMI', 'Weight', 'Height']]

df_cleaned=df_cleaned.drop(41)
df_cleaned=df_cleaned.drop(38)

num_rows, num_cols = df_cleaned.shape
num_rows

df_cleaned.to_csv('cleaned_data.csv',index=False)

height_outliers = df_cleaned[(df_cleaned['Height'] < 55) | (df_cleaned['Height'] > 85)]
height_outliers
weight_outliers = df_cleaned[(df_cleaned['Weight'] < 120) | (df_cleaned['Weight'] > 350)]
weight_outliers

import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
cd = pd.read_csv('cleaned_data.csv')
cd.head()

cd = cd.iloc[:, :-2]
cd.to_csv('cleaned_data_modified.csv',index=False)

import seaborn as sns

sns.boxplot(data=cd[['Weight', 'Abdomen']])

import statsmodels.api as sm

# variabila dependenta si variabilele independente
y = cd['BodyFat']
X = cd.drop(columns=['BodyFat', 'Density'])  # elimin 'BodyFat' si 'Density' deoarece 'Density' este strans legat de 'BodyFat'

# coloana pentru intercept
X = sm.add_constant(X)

# regresie liniara folosind OLS
model = sm.OLS(y, X).fit()

model_summary = model.summary()
model_summary

from itertools import combinations

# functie pentru calcularea si compararea modelului de regresie liniara pentru toate combinatiile de doi predictori
def calculatebesttwopredictormodel(X, y):
    # stocam rezultatele pentru fiecare model
    models_results = []

    # generam toate combinatiile de 2 predictori
    predictor_combinations = combinations(X.columns[1:], 2)

    for combination in predictor_combinations:

        X_selected = X[list(combination) + ['const']]
        model = sm.OLS(y, X_selected).fit()

        models_results.append({
            'predictors': combination,
            'rsquared': model.rsquared,
            'adjr': model.rsquared_adj,
            'aic': model.aic,
            'bic': model.bic
        })

    models_cd = pd.DataFrame(models_results)
    models_cd_sorted = models_cd.sort_values(by=['aic', 'bic'])

    return models_cd_sorted


best_models_cd = calculate_best_two_predictor_model(X, y)


best_models_cd.head()

import statsmodels.api as sm

X = cd[['Abdomen', 'Weight']]
y = cd['BodyFat']

X = sm.add_constant(X)

model = sm.OLS(y, X).fit()

intercept = model.params['const']
coef_abdomen = model.params['Abdomen']
coef_weight = model.params['Weight']

print(f"Intercept: {intercept}")
print(f"Coefficient for Abdomen: {coef_abdomen}")
print(f"Coefficient for Weight: {coef_weight}")

from matplotlib import pyplot as plt
model_summary = model.summary()
print(model_summary)

# reziduurile
residuals = y - model.fittedvalues

plt.figure(figsize=(10, 6))
plt.hist(residuals, bins=20, edgecolor='k')
plt.title('Histogramă pentru reziduuri')
plt.xlabel('Reziduu')
plt.ylabel('Frecvență')
plt.show()

plt.figure(figsize=(10, 6))
plt.scatter(model.fittedvalues, residuals)
plt.hlines(0, xmin=model.fittedvalues.min(), xmax=model.fittedvalues.max(), colors='r', linestyles='dashed')
plt.title('Reziduuri vs. Valorile Prezise')
plt.xlabel('Valorile Prezise')
plt.ylabel('Reziduu')
plt.show()

from statsmodels.graphics.regressionplots import plot_leverage_resid2

fig, ax = plt.subplots(figsize=(12, 8))
fig = plot_leverage_resid2(model, ax=ax)
plt.title('Leverage vs. Reziduuri')
plt.show()

